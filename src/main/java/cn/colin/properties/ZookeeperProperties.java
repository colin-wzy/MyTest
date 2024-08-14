package cn.colin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zookeeper")
@Data
public class ZookeeperProperties {
    private String connectionString;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private Retry retry;

    @Data
    public static class Retry {
        private int baseSleepTimeMs;
        private int maxRetries;
    }
}
