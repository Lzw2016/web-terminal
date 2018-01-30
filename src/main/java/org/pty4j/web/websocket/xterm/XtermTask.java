package org.pty4j.web.websocket.xterm;

import com.google.common.collect.Maps;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;
import org.pty4j.web.websocket.Task;
import org.pty4j.web.websocket.TaskType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-30 11:05 <br/>
 */
@SuppressWarnings("Duplicates")
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class XtermTask extends Task {

    private String taskId = UUID.randomUUID().toString();
    private PtyProcess process;
    private BufferedWriter outputWriter;
    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    /**
     * 初始化终端 - 失败返回null
     */
    public static XtermTask newXtermTask(WebSocketSession session, String taskId) {
        XtermTask xtermTask = new XtermTask();
        xtermTask.addWebSocketSession(session);
        if (taskId != null) {
            xtermTask.taskId = String.format("[%1$s]-%2$s", taskId, UUID.randomUUID().toString());
        }
        try {
            xtermTask.initializeProcess();
        } catch (IOException e) {
            log.error("初始化终端失败", e);
            return null;
        }
        return xtermTask;
    }

    /**
     * 初始化 Terminal
     */
    private void initializeProcess() throws IOException {
        if (process != null) {
            return;
        }
        Map<String, String> envs = Maps.newHashMap(System.getenv());
        String[] termCommand;
        if (Platform.isWindows()) {
            termCommand = new String[]{"cmd.exe"};
        } else {
            termCommand = new String[]{"/bin/bash", "--login"};
            envs.put("TERM", "xterm");
        }
        String userHome = System.getProperty("user.home");
        process = PtyProcess.exec(termCommand, envs, userHome, false, false, null);
        process.setWinSize(new WinSize(80, 10));
        new Thread(() -> printReader(process.getInputStream(),"stdout")).start(); // stdout
        new Thread(() -> printReader(process.getErrorStream(),"stdout")).start(); // stderr
        outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    /**
     * 打印输出到终端
     */
    private void printReader(InputStream inputStream,String stdType) {
        try {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                sendMessage(new String[]{stdType, new String(data, 0, nRead)});
            }
        } catch (Exception e) {
            log.error("Terminal Print Error", e);
        }
    }

    /**
     * 处理输入命令
     */
    public void onCommand(String command) throws InterruptedException {
        if (command == null) {
            return;
        }
        commandQueue.put(command);
        new Thread(() -> {
            try {
                String cmd;
                while ((cmd = commandQueue.poll()) != null) {
                    outputWriter.write(cmd);
                }
                outputWriter.flush();
            } catch (IOException e) {
                log.error("指令输入失败", e);
            }
        }).start();
    }

    /**
     * 改变终端大小
     */
    public void onTerminalResize(int columns, int rows) {
        if (process != null) {
            process.setWinSize(new WinSize(columns, rows));
        }
    }

    @Override
    public void run() {
        if (process != null) {
            try {
                int returnCode = process.waitFor();
                log.info("退出代码 [{}]", returnCode);
            } catch (InterruptedException e) {
                log.error("等待退出中断", e);
            }
        }
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public void destroyTask() throws IOException {
        if (process != null) {
            if (process.getInputStream() != null) {
                process.getInputStream().close();
            }
            if (process.getErrorStream() != null) {
                process.getErrorStream().close();
            }
            if (process.getOutputStream() != null) {
                process.getOutputStream().close();
            }
            process.destroy();
        }
        if (outputWriter != null) {
            outputWriter.close();
        }
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.XtermTask;
    }
}
