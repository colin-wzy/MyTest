package cn.colin.handler;

import cn.colin.common.response.Response;
import cn.colin.exceptions.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Response<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed("missing parameter: " + exception.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Response<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed("parameter type mismatch: " + exception.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Response<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed("request body is invalid");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("validation failed");
        return Response.failed(message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Response<?> bindExceptionHandler(BindException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("validation failed");
        return Response.failed(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Response<?> constraintViolationExceptionHandler(ConstraintViolationException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return Response.failed(message);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Response<?> businessExceptionHandler(BusinessException exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed(exception.getCode(), exception.getMessage());
    }

    /**
     * 异常消息统一处理
     *
     * @param exception     异常类型
     * @param handlerMethod 抛出异常的方法类
     * @return Response
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception exception, HandlerMethod handlerMethod) {
        log.error("{}.{} error", handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName(), exception);
        return Response.failed(exception.getMessage());
    }
}