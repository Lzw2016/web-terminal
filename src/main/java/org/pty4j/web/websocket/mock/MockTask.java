package org.pty4j.web.websocket.mock;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.pty4j.web.websocket.Task;
import org.pty4j.web.websocket.TaskType;
import org.pty4j.web.websocket.mock.git.GitProgressMonitor;
import org.pty4j.web.websocket.mock.git.GitUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-30 22:01 <br/>
 */
@SuppressWarnings("Duplicates")
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class MockTask extends Task {
    private String taskId = UUID.randomUUID().toString();
    private StringBuilder logText = new StringBuilder();

    /**
     * 初始化终端 - 失败返回null
     */
    public static MockTask newXtermTask(WebSocketSession session, String taskId) {
        MockTask mockTask = new MockTask();
        mockTask.addWebSocketSession(session);
        if (taskId != null) {
            mockTask.taskId = String.format("[%1$s]-%2$s", taskId, UUID.randomUUID().toString());
        }
        return mockTask;
    }

    /**
     * 打印各种数据 - 测试终端
     */
    @Override
    public void run() {
        git();
    }

    /**
     * 测试Git下载
     */
    private void git() {
        String directory = "G:\\CodeDownloadPath\\" + UUID.randomUUID().toString();
        String repositoryUrl = "https://github.com/spotify/docker-client.git";
        String commitId = "9bc725cb7f3e78fe635cae7ae43a1443072e2871";
        ProgressMonitor progressMonitor = new GitProgressMonitor(msg -> printReader(msg, "stdout"));
        GitUtils.downloadCode(directory, repositoryUrl, commitId, progressMonitor);
        printReader("\r\nMockTask 退出", "stdout");
    }

    /**
     * 打印输出到终端 stdout stderr
     */
    private void printReader(String msg, String stdType) {
        sendMessage(new String[]{stdType, msg});
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public void destroyTask() {
        closeAllSession();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.MockTask;
    }
}
