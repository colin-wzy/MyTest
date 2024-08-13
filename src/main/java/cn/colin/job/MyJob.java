package cn.colin.job;

import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class MyJob implements Job {
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.println("Executing MyJob at " + jobExecutionContext.getScheduler().getSchedulerName());
    }
}
