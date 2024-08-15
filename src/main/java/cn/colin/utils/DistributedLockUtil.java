package cn.colin.utils;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class DistributedLockUtil {
    private static CuratorFramework client;
    private static StringRedisTemplate redisTemplate;

    @Resource
    public void setClient(CuratorFramework client) {
        DistributedLockUtil.client = client;
    }

    @Resource
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        DistributedLockUtil.redisTemplate = redisTemplate;
    }

    public static InterProcessMutex getLock(String lockPath) {
        return new InterProcessMutex(client, lockPath);
    }

    /**
     * 获取ZK锁，可重入
     *
     * @param lock     锁
     * @param time     等待时间
     * @param timeUnit 时间单位
     * @return 如果成功获取锁，返回true；否则返回false
     */
    @SneakyThrows
    public static boolean acquireZkLock(InterProcessMutex lock, long time, TimeUnit timeUnit) {
        return lock.acquire(time, timeUnit);
    }

    /**
     * 释放ZK锁
     *
     * @param lock 锁
     */
    @SneakyThrows
    public static void releaseZkLock(InterProcessMutex lock) {
        if (lock.isAcquiredInThisProcess()) {
            lock.release();
        }
    }

    /**
     * 获取redis锁，不可重入
     *
     * @param lockKey    锁key
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return 如果成功获取锁，返回true；否则返回false
     */
    public static boolean acquireRedisLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        String lockValue = String.valueOf(Thread.currentThread().getId());
        Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, timeUnit);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放redis锁，对应不可重入锁
     *
     * @param lockKey 锁key
     * @return 如果成功释放，返回true；否则返回false
     */
    public static boolean releaseRedisLock(String lockKey) {
        String lockValue = String.valueOf(Thread.currentThread().getId());
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
        return result != null && result == 1L;
    }
}