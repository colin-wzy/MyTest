package cn.colin.service.impl;

//import cn.colin.db.DataSource;
//import cn.colin.db.DataSourceConfig;

import cn.colin.common.Response;
import cn.colin.service.UserService;
import cn.colin.ws.NotificationWebSocketHandler;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.colin.utils.TokenUtil;
import cn.colin.redis.RedisMessagePublisher;
import cn.colin.request.LoginRequest;
import cn.colin.entity.User;
import cn.colin.mapper.UserMapper;
import cn.colin.utils.JsonUtil;
import cn.colin.utils.JwtUtil;
import cn.colin.utils.UserUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public String login(LoginRequest request) {
        String userName = request.getUserName();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, request.getPwd());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        User user = (User) authentication.getPrincipal();
        redisTemplate.opsForValue().set(userName, JsonUtil.toJsonString(user), 5, TimeUnit.MINUTES);
        String token = JwtUtil.createJwt(userName, 300L);
        TokenUtil.putToken(userName, token);
        return token;
    }

    @Override
    public String refreshToken(String token) {
        try {
            JwtUtil.verify(token);
            return token;
        } catch (TokenExpiredException e) {
            // 过期生成新token
            DecodedJWT jwt = JwtUtil.parseJWT(token);
            String userName = jwt.getSubject();
            User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
            if (user == null) {
                throw new RuntimeException("user not found");
            }
            redisTemplate.opsForValue().set(userName, JsonUtil.toJsonString(user), 5, TimeUnit.MINUTES);
            String newToken = JwtUtil.createJwt(userName, 300L);
            TokenUtil.putToken(userName, newToken);
            return newToken;
        } catch (JWTVerificationException e) {
            // jwt校验不通过
            return null;
        }
    }

    @Override
    public void logout() {
        User user = UserUtil.getUser();
        if (user != null) {
            TokenUtil.removeToken(user.getUserName());
            redisTemplate.delete(user.getUserName());
        }
    }

    @SneakyThrows
    @Override
    public User findUser(String userId) {
        //TODO 这里sleep测试@Cacheable注解是否生效
        Thread.sleep(2000L);
        return userMapper.selectById(userId);
    }

    @Override
    public User findCurrentUser() {
        //TODO 使用redis的发布订阅
        User user = UserUtil.getUser();
        RedisMessagePublisher.publish(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(User user) {
        String pwd = user.getPwd();
        user.setPwd(passwordEncoder.encode(pwd));
        userMapper.insert(user);
    }
}
