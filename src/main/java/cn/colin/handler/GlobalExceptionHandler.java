package cn.colin.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import cn.colin.common.response.Response;

/**
 * @author Administrator
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Response<?> accessDeniedExceptionHandler(AccessDeniedException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed("access denied");
    }

    /**
     * 异常消息统一处理
     *
     * @param exception     异常类型
     * @param handlerMethod 抛出异常的方法类
     * @return BaseResult
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed(exception.getMessage());
    }
}
