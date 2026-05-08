package cn.colin.job;

import lombok.extern.slf4j.Slf4j;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class MyJob implements Job {
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext ctx) {
        log.info("执行定时任务 MyJob, scheduler={}", ctx.getScheduler().getSchedulerName());
    }
}
