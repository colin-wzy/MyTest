package cn.colin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import cn.colin.common.response.Response;

/**
 * 处理请求头不该携带token的接口，携带了token，由SecurityConfig/anonymous()决定接口
 *
 * @author Administrator
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        Response.failed(response, "access denied");
    }
}
