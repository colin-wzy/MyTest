package cn.colin.utils;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DistributedLockUtil {
    private static CuratorFramework client;

    @Resource
    public void setClient(CuratorFramework client) {
        DistributedLockUtil.client = client;
    }

    public static InterProcessMutex getLock(String lockPath) {
        return new InterProcessMutex(client, lockPath);
    }

    /**
     * 获取锁，带超时
     *
     * @param lock     锁
     * @param time     等待时间
     * @param timeUnit 时间单位
     * @return 如果成功获取锁，返回true；否则返回false
     */
    @SneakyThrows
    public static boolean acquire(InterProcessMutex lock, long time, TimeUnit timeUnit) {
        return lock.acquire(time, timeUnit);
    }

    /**
     * 释放锁
     *
     * @param lock 锁
     */
    @SneakyThrows
    public static void release(InterProcessMutex lock) {
        if (lock.isAcquiredInThisProcess()) {
            lock.release();
        }
    }
}