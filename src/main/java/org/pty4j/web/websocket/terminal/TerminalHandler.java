package org.pty4j.web.websocket.terminal;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.utils.mapper.JacksonMapper;
import org.clever.common.utils.validator.BaseValidatorUtils;
import org.clever.common.utils.validator.ValidatorFactoryUtils;
import org.fusesource.jansi.Ansi;
import org.pty4j.web.dto.request.TerminalReq;
import org.pty4j.web.dto.response.TerminalRes;
import org.pty4j.web.websocket.Handler;
import org.pty4j.web.websocket.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-29 11:25 <br/>
 */
@Component
@Slf4j
public class TerminalHandler extends Handler {

    private static int COUNTER = 1;

    /**
     * 连接成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("建立连接");
        TerminalTask terminalTask = TerminalTask.newTerminalTask(session, String.valueOf(COUNTER++));
        if (terminalTask == null) {
            sendErrorMessage(session, "Terminal 初始化失败");
            return;
        }
        putAndStartTask(terminalTask);
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
        TerminalTask terminalTask = null;
        for (Task task : getAllTask()) {
            if (task.contains(session) && task instanceof TerminalTask) {
                terminalTask = (TerminalTask) task;
                break;
            }
        }
        if (terminalTask == null) {
            sendErrorMessage(session, "未初始化 Terminal");
            return;
        }
        TerminalReq terminalReq = JacksonMapper.nonEmptyMapper().fromJson(message.getPayload(), TerminalReq.class);
        if (terminalReq == null) {
            sendErrorMessage(session, "请求数据解析失败");
            return;
        }
        // 校验参数 TerminalReq 的完整性
        try {
            BaseValidatorUtils.validateThrowException(ValidatorFactoryUtils.getHibernateValidator(), terminalReq);
        } catch (ConstraintViolationException e) {
            log.info("请求参数校验失败", e);
            sendErrorMessage(session, JacksonMapper.nonEmptyMapper().toJson(BaseValidatorUtils.extractPropertyAndMessageAsList(e, ",")));
            return;
        }
        if (terminalReq.getType() == null) {
            return;
        }
        // 处理请求数据
        switch (terminalReq.getType()) {
            case TerminalReq.TERMINAL_COMMAND:
                terminalTask.onCommand(terminalReq.getCommand());
                break;
            case TerminalReq.TERMINAL_RESIZE:
                terminalTask.onTerminalResize(terminalReq.getColumns(), terminalReq.getRows());
                break;
            default:
                log.info("不支持的操作 [{}]", message.getPayload());
        }
    }
}
