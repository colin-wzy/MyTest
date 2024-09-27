package cn.colin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import cn.colin.common.response.Response;

/**
 * 处理请求头该携带token的接口，没有携带token，由SecurityConfig/authenticated()决定接口
 *
 * @author Administrator
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        Response.failed(response, "token not found");
    }
}
