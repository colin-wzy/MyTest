package cn.colin.limit;

import cn.colin.common.response.Response;
import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimiterAspect {

    // 存储不同方法的 RateLimiter 实例
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimited)")
    public Object limitRate(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 生成唯一的 key 用于存储 RateLimiter
        String methodKey = signature.getDeclaringTypeName() + "." + signature.getName();
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(methodKey, key -> RateLimiter.create(rateLimited.value()));
        if (rateLimiter.tryAcquire()) {
            return joinPoint.proceed();
        } else {
            return Response.failed("RateLimited");
        }
    }
}
