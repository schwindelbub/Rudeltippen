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
import services.I18nService;

import com.google.inject.Inject;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class StartupActions {
    private static final Logger LOG = LoggerFactory.getLogger(StartupActions.class);
    private static final String RESULTSCRON = "0 */4 * * * ?";
    private static final String REMINDERCRON = "0 0 */1 * * ?";
    private static final String KICKOFFCRON = "0 0 4 * * ?";
    private static final String GAMETIPCRON = "0 */1 * * * ?";
    private static final String TRIGGER_GROUP = "triggerGroup";
    private static final String JOB_GROUP = "jobGroup";

    @Inject
    private AppJobFactory appJobFactory;

    @Inject
    private DataService dataService;
    
    @Inject
    private I18nService i18nService;

    @Start(order=100)
    public void startup() {
        if (NinjaConstant.MODE_TEST.equals(System.getProperty(NinjaConstant.MODE_KEY_NAME))) {
            return;
        }

        initJobs();
        scheduleJobs();
    }

    private void initJobs() {
        List<AbstractJob> abstractJobs = new ArrayList<AbstractJob>();
        abstractJobs.add(new AbstractJob(Constants.GAMETIPJOB.get(), i18nService.get("job.gametipjob.executed"), i18nService.get("job.gametipjob.description")));
        abstractJobs.add(new AbstractJob(Constants.KICKOFFJOB.get(), i18nService.get("job.playdayjob.executed"), i18nService.get("job.playdayjob.description")));
        abstractJobs.add(new AbstractJob(Constants.REMINDERJOB.get(), i18nService.get("job.reminderjob.executed"), i18nService.get("job.reminderjob.description")));
        abstractJobs.add(new AbstractJob(Constants.RESULTJOB.get(), i18nService.get("job.resultsjob.executed"), i18nService.get("job.resultsjob.descrption")));

        for (AbstractJob abstractJob : abstractJobs) {
            AbstractJob job = dataService.findAbstractJobByName(abstractJob.getName());
            if (job == null) {
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
                scheduler.scheduleJob(getJobDetail(GameTipJob.class, Constants.GAMETIPJOB.get()), getTrigger("gameTipJobTrigger", GAMETIPCRON));
                scheduler.scheduleJob(getJobDetail(KickoffJob.class, Constants.KICKOFFJOB.get()), getTrigger("kickoffJobTrigger", KICKOFFCRON));
                scheduler.scheduleJob(getJobDetail(ReminderJob.class, Constants.REMINDERJOB.get()), getTrigger("reminderJobTrigger", REMINDERCRON));
                scheduler.scheduleJob(getJobDetail(ResultJob.class, Constants.RESULTJOB.get()), getTrigger("resultsJobTrigger", RESULTSCRON));

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