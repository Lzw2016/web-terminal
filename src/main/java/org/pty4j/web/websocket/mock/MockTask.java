package org.pty4j.web.websocket.mock;

import com.github.dockerjava.api.command.BuildImageCmd;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.fusesource.jansi.Ansi;
import org.pty4j.web.exception.BusinessException;
import org.pty4j.web.websocket.Task;
import org.pty4j.web.websocket.TaskType;
import org.pty4j.web.websocket.mock.docker.BuildImageProgressMonitor;
import org.pty4j.web.websocket.mock.docker.DockerClientUtils;
import org.pty4j.web.websocket.mock.git.GitProgressMonitor;
import org.pty4j.web.websocket.mock.git.GitUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.util.*;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-30 22:01 <br/>
 */
@SuppressWarnings("Duplicates")
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class MockTask extends Task {
    private static DockerClientUtils dockerClientUtils = new DockerClientUtils();

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
//        git();
        docker();
    }

    private void docker() {
        dockerClientUtils.init();
        printReader(Ansi.ansi().fgYellow().a("开始构建 Docker 镜像").newline().reset().toString(), "stdout");
        // 构建镜像 - 整理参数
        Map<String, String> labels = new HashMap<>();
        String imageName = "test1234567890:1.0.0";
        Set<String> tags = new HashSet<>();
        tags.add(imageName);
        String dockerfilePath = FilenameUtils.concat("G:\\CodeDownloadPath\\loan-mall", "./Dockerfile");
        File dockerfile = new File(dockerfilePath);
        if (!dockerfile.exists() || !dockerfile.isFile()) {
            throw new BusinessException(String.format("Dockerfile文件[%1$s]不存在", dockerfilePath));
        }
        String buildImage = dockerClientUtils.execute(client -> {
            // 构建镜像
            BuildImageCmd buildImageCmd = client.buildImageCmd();
            buildImageCmd.withDockerfile(dockerfile);
            buildImageCmd.withLabels(labels);
            buildImageCmd.withTags(tags);
            return buildImageCmd.exec(new BuildImageProgressMonitor(msg -> printReader(msg, "stdout"))).awaitImageId();
        });
        printReader(Ansi.ansi().fgGreen().newline().a("Docker 镜像ID: ").a(buildImage).reset().toString(), "stdout");
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
