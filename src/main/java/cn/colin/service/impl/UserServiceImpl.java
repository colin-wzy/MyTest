package cn.colin.service.impl;

import cn.colin.mapper.SlaveUserMapper;
import cn.colin.service.UserService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cn.colin.utils.TokenUtil;
import cn.colin.common.request.LoginRequest;
import cn.colin.common.request.UpdateUserRequest;
import cn.colin.common.entity.User;
import cn.colin.mapper.UserMapper;
import cn.colin.utils.JsonUtil;
import cn.colin.utils.JwtUtil;
import cn.colin.utils.UserUtil;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import cn.colin.service.RoleService;
import cn.colin.common.entity.Role;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private SlaveUserMapper slaveUserMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private StringRedisTemplate redisTemplate;
    @Resource
    private RoleService roleService;

    private BloomFilter<String> userNameBloomFilter;

    @PostConstruct
    public void initUserName() {
        // 预加载数据库中的所有用户名
        List<String> userNameList = userMapper.selectList(Wrappers.lambdaQuery(User.class)).stream().map(User::getUserName).toList();
        userNameBloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), Math.max(userNameList.size(), 1000), 0.01);
        for (String username : userNameList) {
            userNameBloomFilter.put(username);
        }
    }

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

    @Override
    public User findUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public List<User> findUserByName(String userName) {
        // 布隆过滤器特性：命中了也不一定有，没有就一定没有
        if (!userNameBloomFilter.mightContain(userName)) {
            return Collections.emptyList(); // 快速返回
        }
        // 使用LIKE查询进行模糊搜索
        return userMapper.selectList(Wrappers.lambdaQuery(User.class)
                .like(User::getUserName, userName));
    }

    @Override
    public User findCurrentUser() {
        return UserUtil.getUser();
    }

    @Override
    public List<User> findAllUsers() {
        return userMapper.selectList(Wrappers.lambdaQuery(User.class));
    }

    @Override
    public void addUser(User user) {
        String pwd = user.getPwd();
        user.setPwd(passwordEncoder.encode(pwd));
        userMapper.insert(user);
        userNameBloomFilter.put(user.getUserName());
    }

    @Override
    public void deleteUserById(Long userId) {
        userMapper.deleteById(userId);
    }

    @Override
    public void updateUser(UpdateUserRequest request) {
        User user = new User();
        user.setId(request.getId());
        user.setUserName(request.getUserName());
        user.setRealName(request.getRealName());
        user.setSex(request.getSex());
        if (request.getPwd() != null && !request.getPwd().isEmpty()) {
            user.setPwd(passwordEncoder.encode(request.getPwd()));
        }
        userMapper.updateById(user);
    }

    @Override
    @DSTransactional
    public void testTransactional() {
        User mUser = new User();
        mUser.setUserName("ttt");
        mUser.setPwd(passwordEncoder.encode("ttt"));
        mUser.setRealName("ttt");

        User sUser = new User();
        sUser.setUserName("sss");
        sUser.setPwd(passwordEncoder.encode("sss"));
        sUser.setRealName("sss");

        userMapper.insert(mUser);
        slaveUserMapper.insert(sUser);

        int divisor = 0;
        log.info("Test transactional, result: {}", 1 / divisor);
    }

    @Override
    public void bindRoles(Long userId, List<Long> roleIds) {
        roleService.bindRoles(userId, roleIds);
    }

    @Override
    public List<Role> findUserRoles(Long userId) {
        return roleService.findRolesByUserId(userId);
    }

    @Override
    public List<Role> findCurrentUserRoles() {
        User currentUser = UserUtil.getUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }
        return roleService.findRolesByUserId(currentUser.getId());
    }
}
