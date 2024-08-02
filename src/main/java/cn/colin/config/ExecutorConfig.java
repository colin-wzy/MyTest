package cn.colin.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 */
@Configuration
public class ExecutorConfig {

    @Bean
    public ThreadFactory validTokenThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger threadNum = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r, "ValidToken-thread-" + threadNum.getAndIncrement());
                if (thread.isDaemon()) {
                    thread.setDaemon(false);
                }
                if (thread.getPriority() != Thread.NORM_PRIORITY) {
                    thread.setPriority(Thread.NORM_PRIORITY);
                }
                return thread;
            }
        };
    }

    @Bean
    public ScheduledThreadPoolExecutor validTokenExecutor(ThreadFactory validTokenThreadFactory) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(50, validTokenThreadFactory);
        executor.setRemoveOnCancelPolicy(true);
        executor.setKeepAliveTime(10, TimeUnit.SECONDS);
        return executor;
    }

}
