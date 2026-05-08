package cn.colin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "onlyoffice")
@Data
public class OnlyOfficeProperties {
    private String docServerUrl;
    private String callbackUrl;
    private boolean jwtEnabled;
    private String jwtSecret;
}
