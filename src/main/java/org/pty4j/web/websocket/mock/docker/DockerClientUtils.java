package org.pty4j.web.websocket.mock.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.pty4j.web.exception.BusinessException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * DockerClient 使用工具 - 执行各种Docker操作
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2017-12-22 11:47 <br/>
 */
@Component
@Slf4j
public class DockerClientUtils {

    private DockerClientPool pool;

    /**
     * 初始化连接池
     */
    @PostConstruct
    public void init() {
        if (pool != null) {
            return;
        }
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        builder.withDockerHost("tcp://192.168.159.131:2375");
        builder.withApiVersion("1.33");
        List<DockerClientConfig> clientConfigs = new ArrayList<>();
        clientConfigs.add(builder.build());
        PooledDockerClientFactory dockerClientFactory = new PooledDockerClientFactory(clientConfigs);

        // 连接池配置
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        // 对象池中管理的最多对象个数
        conf.setMaxTotal(8);
        // 对象池中最大的空闲对象个数
        conf.setMaxIdle(8);
        // 对象池中最小的空闲对象个数
        conf.setMinIdle(1);
        pool = new DockerClientPool(dockerClientFactory, conf);
    }

    /**
     * 获取连接池信息
     */
    public void dumpPoolInfo() {
        if (log.isDebugEnabled()) {
            String tmp = "\r\n" +
                    "#=======================================================================================================================#\r\n" +
                    "# ------Dump Pool Info------\r\n" +
                    "#\t 活动连接：" + pool.getNumActive() + "\r\n" +
                    "#\t 空闲连接：" + pool.getNumIdle() + "\r\n" +
                    "#\t 正在使用的连接：" + pool.getNumWaiters() + "\r\n" +
                    "#\t 连接获取总数统计：" + pool.getBorrowedCount() + "\r\n" +
                    "#\t 连接返回总数统计：" + pool.getReturnedCount() + "\r\n" +
                    "#\t 连接创建总数统计：" + pool.getCreatedCount() + "\r\n" +
                    "#\t 连接销毁总数统计：" + pool.getDestroyedCount() + "\r\n" +
                    "#\t 连接销毁(因为连接不可用)总数统计：" + pool.getDestroyedByBorrowValidationCount() + "\r\n" +
                    "#\t 连接销毁(因为连接被回收)总数统计：" + pool.getDestroyedByEvictorCount() + "\r\n" +
                    "#=======================================================================================================================#\r\n";
            log.debug(tmp);
        }
    }

    /**
     * 执行 Docker 操作
     *
     * @param executor 执行回调接口
     */
    public <T> T execute(DockerClientExecutor<T> executor) {
        dumpPoolInfo();
        if (executor == null) {
            return null;
        }
        DockerClient client = null;
        try {
            client = pool.borrowObject();
            return executor.execute(client);
        } catch (Throwable e) {
            log.warn("执行Docker操作失败", e);
            throw new BusinessException("执行Docker操作失败", e);
        } finally {
            if (client != null) {
                pool.returnObject(client);
            }
        }
    }
}
