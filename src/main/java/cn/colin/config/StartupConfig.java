package cn.colin.config;

import cn.colin.utils.MinioUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import cn.colin.entity.User;
import cn.colin.mapper.UserMapper;


/**
 * @author Administrator
 */
@Configuration
public class StartupConfig {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            if (userMapper.selectCount(Wrappers.lambdaQuery(User.class).eq(User::getDFlag, false)) == 0) {
                User user = new User();
                user.setUserName("wangzhongyu");
                user.setRealName("王钟毓");
                user.setPwd(passwordEncoder.encode("123456"));
                userMapper.insert(user);
            }
            if (!MinioUtil.bucketExists("first")) {
                MinioUtil.makeBucket("first");
            }
        };
    }
}
