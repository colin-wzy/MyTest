package cn.colin.config;

import cn.colin.utils.MinioUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import cn.colin.common.entity.User;
import cn.colin.mapper.UserMapper;

import java.net.InetAddress;


/**
 * @author Administrator
 */
@Configuration
@Slf4j
public class StartupConfig {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private Environment env;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            initUserAdmin();
            initMinioBucket();
            printApiUrl();
        };
    }

    private void initUserAdmin() {
        if (userMapper.selectCount(Wrappers.lambdaQuery(User.class).eq(User::getDFlag, false)) == 0) {
            User user = new User();
            user.setUserName("wangzhongyu");
            user.setRealName("王钟毓");
            user.setPwd(passwordEncoder.encode("123456"));
            userMapper.insert(user);
        }
    }

    private void initMinioBucket() {
        if (!MinioUtil.bucketExists("first")) {
            MinioUtil.makeBucket("first");
        }
    }

    @SneakyThrows
    private void printApiUrl() {
        String protocol = "http";
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/doc.html";
        } else {
            contextPath = contextPath + "/doc.html";
        }
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        log.info("""
                        ----------------------------------------------------------
                        \t应用程序 {} 接口文档访问 URL:
                        \t本地: \t{}://localhost:{}{}
                        \t外部: \t{}://{}:{}{}
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath);
    }
}
