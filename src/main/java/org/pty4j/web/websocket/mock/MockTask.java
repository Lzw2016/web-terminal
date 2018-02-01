package org.pty4j.web.websocket.mock;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.clever.common.utils.exception.ExceptionUtils;
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
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
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 100; j++) {
                sb.append(i).append(' ');
            }
            printReader(Ansi.ansi().a(sb.toString()).newline().reset().toString(), "stdout");
        }
//        git();
        docker();
//        dockerLog();
    }

    private void dockerLog() {
        dockerClientUtils.init();
//        String id = dockerClientUtils.execute(client -> {
//            // 创建容器
//            CreateContainerCmd createContainerCmd = client.createContainerCmd("a0ce313a395781940ded9938a593be77bf5b1051d69642472265bc0a7f62df4c");
//            createContainerCmd.withName("admin-demo");
//            Ports ports = new Ports();
//            ports.bind(new ExposedPort(9066), null);
//            createContainerCmd.withPortBindings(ports);
//            createContainerCmd.withPublishAllPorts(true);
//            String containerId = createContainerCmd.exec().getId();
//            // 启动容器
//            client.startContainerCmd(containerId).exec();
//            return containerId;
//        });
        // 监听日志
        ResultCallback resultCallback = dockerClientUtils.execute(client -> {
            LogContainerCmd cmd = client.logContainerCmd("8c9b6e02600ab327973280cc49442782c5e5f0f65ea47bc8d388c33b55f35011");
            cmd.withFollowStream(true);
            cmd.withTimestamps(false);
            cmd.withStdErr(true);
            cmd.withStdOut(true);
//            cmd.withSince(0);
            cmd.withTail(1000);
            // cmd.withTailAll();
            return cmd.exec(new ResultCallback<Frame>() {
                private Closeable closeable;

                @Override
                public void onStart(Closeable closeable) {
                    this.closeable = closeable;
                }

                @Override
                public void onNext(Frame object) {
                    String logs = new String(object.getPayload());
                    String[] array = logs.split("\r\n|\n");
                    for (String log : array) {
                        printReader(Ansi.ansi().a(log).newline().toString(), "stdout");
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.warn("查看日志出现异常", throwable);
                    printReader("\r\n查看日志出现异常\r\n" + ExceptionUtils.getStackTraceAsString(throwable), "stdout");
                }

                @Override
                public void onComplete() {
                    printReader("\r\nDocker容器已停止\r\n", "stdout");
                }

                @Override
                public void close() throws IOException {
                    if (closeable != null) {
                        closeable.close();
                    }
                }
            });
        });
        // 等待所有的连接关闭
        awaitAllSessionClose();
        try {
            resultCallback.close();
        } catch (IOException ignored) {
        }
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
