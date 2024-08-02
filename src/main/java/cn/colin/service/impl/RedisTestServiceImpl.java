package cn.colin.service.impl;

import cn.colin.service.RedisTestService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


/**
 * @author Administrator
 */
@Service
public class RedisTestServiceImpl implements RedisTestService {

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public void addInRedis() {
        redisTemplate.opsForValue().set("cn/colin", "testValue");
    }

    @Override
    public String getInRedis() {
        return redisTemplate.opsForValue().get("cn/colin");
    }

    @Override
    public void deleteInRedis() {
        redisTemplate.delete("cn/colin");
    }
}
