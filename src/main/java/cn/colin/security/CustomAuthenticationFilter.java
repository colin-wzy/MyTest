package cn.colin.security;

import cn.colin.ws.NotificationWebSocketHandler;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import cn.colin.common.Response;
import cn.colin.utils.TokenUtil;
import cn.colin.entity.User;
import cn.colin.utils.JsonUtil;
import cn.colin.utils.JwtUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Administrator
 */
@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        String userName;
        try {
            DecodedJWT jwt = JwtUtil.verify(token);
            userName = jwt.getSubject();
            String lastToken = TokenUtil.getToken(userName);
            // 不存在说明未登录
            if (lastToken == null) {
                response.getWriter().write(JsonUtil.toJsonString(Response.failed(Response.FAILED_CODE, "user not login")));
                return;
            }
            // 判断token是不是最后一次登录的token
            if (!lastToken.equals(token)) {
                // 发送过期消息
                NotificationWebSocketHandler.sendExpireMessage(token);
                response.getWriter().write(JsonUtil.toJsonString(Response.failed(Response.FAILED_CODE, "token has expired")));
                return;
            }
        } catch (TokenExpiredException e) {
            // 发送过期消息
            NotificationWebSocketHandler.sendExpireMessage(token);
            response.getWriter().write(JsonUtil.toJsonString(Response.failed(Response.FAILED_CODE, "token has expired")));
            return;
        } catch (JWTVerificationException e) {
            // jwt校验不通过
            response.getWriter().write(JsonUtil.toJsonString(Response.failed(Response.FAILED_CODE, "token invalid")));
            return;
        }
        String userString = redisTemplate.opsForValue().get(userName);
        if (StringUtils.isEmpty(userString)) {
            response.getWriter().write(JsonUtil.toJsonString(Response.failed(Response.FAILED_CODE, "user not login")));
            return;
        }
        User user = JsonUtil.fromJsonString(userString, User.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, getAuthorities(user));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> getAuthorities(User user) {
        if (user == null) {
            return null;
        }
        //TODO 实际应取user的角色，并赋值
        if ("wangzhongyu".equals(user.getUserName())) {
            List<SimpleGrantedAuthority> list = new ArrayList<>();
            list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return list;
        }
        return null;
    }
}
