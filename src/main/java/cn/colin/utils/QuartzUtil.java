package cn.colin.utils;

import lombok.SneakyThrows;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzUtil {
    private static Scheduler scheduler;

    static {
        try {
            scheduler = new StdSchedulerFactory("quartz.properties").getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static JobKey getJobKey(String jobName, String groupName) {
        return JobKey.jobKey(jobName, groupName);
    }

    public static TriggerKey getTriggerKey(String jobName, String groupName) {
        return TriggerKey.triggerKey(jobName + "Trigger", groupName);
    }

    @SneakyThrows
    public static void addJob(String jobName, String groupName, Class<? extends Job> jobClass, String cronExpression) {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(getJobKey(jobName, groupName))
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(jobName, groupName))
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    @SneakyThrows
    public static void updateJobCron(String jobName, String groupName, String newCronExpression) {
        TriggerKey triggerKey = getTriggerKey(jobName, groupName);
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);

        if (oldTrigger != null) {
            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(newCronExpression))
                    .build();
            scheduler.rescheduleJob(triggerKey, newTrigger);
        }
    }

    @SneakyThrows
    public static void pauseJob(String jobName, String groupName) {
        scheduler.pauseJob(getJobKey(jobName, groupName));
    }

    @SneakyThrows
    public static void deleteJob(String jobName, String groupName) {
        scheduler.deleteJob(getJobKey(jobName, groupName));
    }

    @SneakyThrows
    public static void resumeJob(String jobName, String groupName) {
        scheduler.resumeJob(getJobKey(jobName, groupName));
    }

    @SneakyThrows
    public static boolean checkJobExists(String jobName, String groupName) {
        return scheduler.checkExists(getJobKey(jobName, groupName));
    }
}
