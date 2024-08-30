package cn.colin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "es")
@Data
public class ElasticsearchProperties {
    private String host;
    private int port;
    private String scheme;
    private boolean enable;
}
