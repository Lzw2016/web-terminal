package org.pty4j.web.websocket.mock;

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
 * 创建时间：2018-01-30 22:01 <br/>
 */
@SuppressWarnings("Duplicates")
@Component
@Slf4j
public class MockHandler extends Handler {
    /**
     * 连接成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("建立连接");
        MockTask mockTask = MockTask.newXtermTask(session, String.valueOf(getCountAndAdd()));
        if (mockTask == null) {
            sendErrorMessage(session, "mock 初始化失败");
            return;
        }
        putAndStartTask(mockTask);
        log.info("mock 初始化完成");
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
        MockTask mockTask = null;
        for (Task task : getAllTask()) {
            if (task.contains(session) && task instanceof MockTask) {
                mockTask = (MockTask) task;
                break;
            }
        }
        if (mockTask == null) {
            sendErrorMessage(session, "未初始化 mock");
            return;
        }
        String[] req = JacksonMapper.nonEmptyMapper().fromJson(message.getPayload(), String[].class);
        if (req == null || req.length < 2) {
            sendErrorMessage(session, "请求数据解析失败");
            return;
        }
        // 处理请求数据
        switch (req[0]) {
//            case "stdin":
//                break;
//            case "set_size":
//                break;
            default:
                log.info("不支持的操作 [{}]", message.getPayload());
        }
    }
}
