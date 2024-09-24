package cn.colin.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import cn.colin.common.entity.User;
import cn.colin.mapper.UserMapper;


/**
 * @author Administrator
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String pwd = authentication.getCredentials().toString();
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        if (!passwordEncoder.matches(pwd, user.getPwd())) {
            throw new RuntimeException("password error");
        }
        return new UsernamePasswordAuthenticationToken(user, pwd, null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
