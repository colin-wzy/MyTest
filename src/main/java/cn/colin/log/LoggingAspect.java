package cn.colin.log;

import cn.colin.common.entity.User;
import cn.colin.redis.RedisMessagePublisher;
import cn.colin.utils.UserUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Around("@annotation(loggingOperation)")
    public Object logAround(ProceedingJoinPoint joinPoint, LoggingOperation loggingOperation) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        User user = UserUtil.getUser();
        String publishMessage = String.format("Class: %s, Method: %s%s", className, methodName,
                (user != null) ? ", UserName: " + user.getUserName() : "");
        // 发布到 Redis
        RedisMessagePublisher.publish(publishMessage);
        return joinPoint.proceed();
    }
}
