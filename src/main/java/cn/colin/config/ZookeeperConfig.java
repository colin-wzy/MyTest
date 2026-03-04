package cn.colin.config;

import cn.colin.properties.ZookeeperProperties;
import jakarta.annotation.Resource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {
    @Resource
    private ZookeeperProperties zooKeeperProperties;

    @Bean
    @ConditionalOnProperty(name = "zookeeper.enable", havingValue = "true", matchIfMissing = true)
    public CuratorFramework curatorFramework() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
                zooKeeperProperties.getRetry().getBaseSleepTimeMs(),
                zooKeeperProperties.getRetry().getMaxRetries()
        );
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                zooKeeperProperties.getConnectionString(),
                zooKeeperProperties.getSessionTimeoutMs(),
                zooKeeperProperties.getConnectionTimeoutMs(),
                retryPolicy
        );
        client.start();
        return client;
    }
}
