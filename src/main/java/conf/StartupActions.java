package conf;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import jobs.AppJobFactory;
import jobs.CleanupJob;
import jobs.GameTipJob;
import jobs.PlaydayJob;
import jobs.ReminderJob;
import jobs.ResultsJob;
import models.AbstractJob;
import ninja.lifecycle.Start;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;

import com.google.inject.Inject;

@Singleton
public class StartupActions {
    private static final Logger LOG = LoggerFactory.getLogger(StartupActions.class);
    private static final String TRIGGER_GROUP = "triggerGroup";
    private static final String JOB_GROUP = "jobGroup";

    @Inject
    private AppJobFactory appJobFactory;

    @Inject
    private DataService dataService;

    @Start(order=100)
    public void startup() {
        initJobs();
        scheduleJobs();
    }

    private void initJobs() {
        List<String> jobNames = new ArrayList<String>();
        jobNames.add("CleanupJob");
        jobNames.add("GameTipJob");
        jobNames.add("PlaydayJob");
        jobNames.add("ReminderJob");
        jobNames.add("ResultsJob");

        for (String jobName : jobNames) {
            AbstractJob abstractJob = dataService.findAbstractJobByName(jobName);
            if (abstractJob == null) {
                abstractJob = new AbstractJob();
                abstractJob.setActive(true);
                abstractJob.setName(jobName);
                dataService.save(abstractJob);
            }
        }
    }

    private void scheduleJobs() {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
            scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            LOG.error("Failed to get scheduler", e);
        }

        if (scheduler != null) {
            try {
                scheduler.setJobFactory(appJobFactory);
                scheduler.scheduleJob(getJobDetail(CleanupJob.class, "cleanupJob"), getTrigger("cleanupJobTrigger", "0 0 2 * * ?"));
                scheduler.scheduleJob(getJobDetail(GameTipJob.class, "gameTipJob"), getTrigger("gameTipJobTrigger", "*/1 * * * * ?"));
                scheduler.scheduleJob(getJobDetail(PlaydayJob.class, "playdayJob"), getTrigger("playdayJobTrigger", "0 0 5 * * ?"));
                scheduler.scheduleJob(getJobDetail(ReminderJob.class, "reminderJob"), getTrigger("reminderJobTrigger", "0 0 1 * * ?"));
                scheduler.scheduleJob(getJobDetail(ResultsJob.class, "resultsJob"), getTrigger("resultsJobTrigger", "*/4 * * * * ?"));

                scheduler.start();

                if (scheduler.isStarted()) {
                    LOG.info("Successfully started quartz scheduler");
                } else {
                    LOG.error("Scheduler is not started!");
                }
            } catch (SchedulerException e) {
                LOG.error("Failed to start scheduler", e);
            }
        }
    }

    private Trigger getTrigger(String identity, String schedule) {
        Trigger birthDayTrigger = newTrigger()
                .withIdentity(identity, TRIGGER_GROUP)
                .withSchedule(cronSchedule(schedule))
                .build();

        return birthDayTrigger;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JobDetail getJobDetail(Class clazz, String identity) {
        JobDetail birthDayJob = newJob(clazz)
                .withIdentity(identity, JOB_GROUP)
                .build();

        return birthDayJob;
    }
}