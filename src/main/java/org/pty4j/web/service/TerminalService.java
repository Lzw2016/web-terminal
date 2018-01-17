package org.pty4j.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Platform;
import org.pty4j.PtyProcess;
import org.pty4j.WinSize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Scope("prototype")
public class TerminalService {

    @Value("${shell:#{null}}")
    private String shellStarter;

    private boolean isReady;
    private String[] termCommand;
    private PtyProcess process;
    private Integer columns = 20;
    private Integer rows = 10;
    private InputStream inputReader;
    private InputStream errorReader;
    private BufferedWriter outputWriter;
    private WebSocketSession webSocketSession;

    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public void onTerminalInit() {

    }

    public void onTerminalReady() {
        new Thread(() -> {
            isReady = true;
            try {
                initializeProcess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initializeProcess() throws Exception {
        if (Platform.isWindows()) {
            this.termCommand = "cmd.exe".split("\\s+");
        } else {
            this.termCommand = "/bin/bash -i".split("\\s+");
        }

        if (Objects.nonNull(shellStarter)) {
            this.termCommand = shellStarter.split("\\s+");
        }
        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm");
        String userHome = System.getProperty("user.home");
        this.process = PtyProcess.exec(termCommand, envs, userHome);

        process.setWinSize(new WinSize(columns, rows));
        this.inputReader = process.getInputStream();
        this.errorReader = process.getErrorStream();
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        new Thread(() -> printReader(inputReader)).start();
        new Thread(() -> printReader(errorReader)).start();
        process.waitFor();
    }

    public void print(String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("type", "TERMINAL_PRINT");
        map.put("text", text);
        String message = new ObjectMapper().writeValueAsString(map);
        webSocketSession.sendMessage(new TextMessage(message));
    }

    private void printReader(InputStream inputStream) {
        try {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                print(new String(data, 0, nRead));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCommand(String command) throws InterruptedException {
        if (Objects.isNull(command)) {
            return;
        }
        commandQueue.put(command);
        new Thread(() -> {
            try {
                outputWriter.write(commandQueue.poll());
                outputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onTerminalResize(String columns, String rows) {
        if (Objects.nonNull(columns) && Objects.nonNull(rows)) {
            this.columns = Integer.valueOf(columns);
            this.rows = Integer.valueOf(rows);

            if (Objects.nonNull(process)) {
                process.setWinSize(new WinSize(this.columns, this.rows));
            }

        }
    }

    public void destroy() {
        if (process != null) {
            process.destroy();
        }
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }
}
