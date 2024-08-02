package cn.colin.redis;

import cn.colin.constants.StringConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import cn.colin.utils.JsonUtil;


/**
 * @author Administrator
 */
@Service
public class RedisMessagePublisher {
    private static StringRedisTemplate redisTemplate;

    @Resource
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        RedisMessagePublisher.redisTemplate = redisTemplate;
    }

    public static void publish(Object message) {
        redisTemplate.convertAndSend(StringConstants.REDIS_TOPIC, message instanceof String ?
                message : JsonUtil.toJsonString(message));
    }
}
