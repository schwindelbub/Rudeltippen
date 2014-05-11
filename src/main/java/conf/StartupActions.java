package conf;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import jobs.AppJobFactory;
import jobs.GameTipJob;
import jobs.KickoffJob;
import jobs.ReminderJob;
import jobs.ResultJob;
import models.AbstractJob;
import models.enums.Constants;
import ninja.lifecycle.Start;
import ninja.utils.NinjaConstant;

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

/**
 * 
 * @author svenkubiak
 *
 */
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
        if (NinjaConstant.MODE_TEST.equals(System.getProperty(NinjaConstant.MODE_KEY_NAME))) {
            return;
        }

        initJobs();
        scheduleJobs();
    }

    private void initJobs() {
        List<String> jobNames = new ArrayList<String>();
        jobNames.add(Constants.GAMETIPJOB.get());
        jobNames.add(Constants.KICKOFFJOB.get());
        jobNames.add(Constants.REMINDERJOB.get());
        jobNames.add(Constants.RESULTJOB.get());

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
                scheduler.scheduleJob(getJobDetail(GameTipJob.class, Constants.GAMETIPJOB.get()), getTrigger("gameTipJobTrigger", "0 */1 * * * ?"));
                scheduler.scheduleJob(getJobDetail(KickoffJob.class, Constants.KICKOFFJOB.get()), getTrigger("kickoffJobTrigger", "0 0 5 * * ?"));
                scheduler.scheduleJob(getJobDetail(ReminderJob.class, Constants.REMINDERJOB.get()), getTrigger("reminderJobTrigger", "0 0 */1 * * ?"));
                scheduler.scheduleJob(getJobDetail(ResultJob.class, Constants.RESULTJOB.get()), getTrigger("resultsJobTrigger", "0 */4 * * * ?"));

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
        return newTrigger()
                .withIdentity(identity, TRIGGER_GROUP)
                .withSchedule(cronSchedule(schedule))
                .build();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JobDetail getJobDetail(Class clazz, String identity) {
        return newJob(clazz)
                .withIdentity(identity, JOB_GROUP)
                .build();
    }
}