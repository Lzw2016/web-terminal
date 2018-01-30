package org.pty4j.web.websocket.xterm;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.fusesource.jansi.Ansi;
import org.pty4j.web.dto.response.TerminalRes;
import org.pty4j.web.websocket.Handler;
import org.pty4j.web.websocket.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-30 11:04 <br/>
 */
@SuppressWarnings("Duplicates")
@Component
@Slf4j
public class XtermHandler extends Handler {
    /**
     * 连接成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("建立连接");
        XtermTask xtermTask = XtermTask.newXtermTask(session, String.valueOf(getCountAndAdd()));
        if (xtermTask == null) {
            sendErrorMessage(session, "Terminal 初始化失败");
            return;
        }
        putAndStartTask(xtermTask);
        log.info("Terminal 初始化完成");
    }

    /**
     * 发送异常消息
     */
    @Override
    protected void sendErrorMessage(WebSocketSession session, String errorMessage) {
        errorMessage = Ansi.ansi().fgRed().a(errorMessage).reset().newline().toString();
        TextMessage textMessage = new TextMessage(JacksonMapper.nonEmptyMapper().toJson(new TerminalRes(errorMessage)));
        try {
            session.sendMessage(textMessage);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 处理消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 找出对应 Task
        XtermTask xtermTask = null;
        for (Task task : getAllTask()) {
            if (task.contains(session) && task instanceof XtermTask) {
                xtermTask = (XtermTask) task;
                break;
            }
        }
        if (xtermTask == null) {
            sendErrorMessage(session, "未初始化 Terminal");
            return;
        }
        String[] req = JacksonMapper.nonEmptyMapper().fromJson(message.getPayload(), String[].class);
        if (req == null || req.length < 2) {
            sendErrorMessage(session, "请求数据解析失败");
            return;
        }
        // 处理请求数据
        switch (req[0]) {
            case "stdin":
                xtermTask.onCommand(req[1]);
                break;
            case "set_size":
                if (req.length >= 3) {
                    xtermTask.onTerminalResize(Integer.parseInt(req[2]), Integer.parseInt(req[1]));
                }
                break;
            default:
                log.info("不支持的操作 [{}]", message.getPayload());
        }
    }
}
