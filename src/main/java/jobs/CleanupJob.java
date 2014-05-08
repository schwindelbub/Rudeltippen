package jobs;

import java.util.List;

import models.AbstractJob;
import models.Confirmation;
import models.ExtraTip;
import models.GameTip;
import models.User;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;
import services.ResultService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class CleanupJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(CleanupJob.class);

    @Inject
    private DataService dataService;
    
    @Inject
    private ResultService resultService;

    public CleanupJob() {
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (resultService.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName("CleanupJob");
            if (job != null && job.isActive()) {
                LOG.info("Started Job: CleanupJob");
                final List<Confirmation> confirmations = dataService.findAllPendingActivatations();
                for (final Confirmation confirmation : confirmations) {
                    final User user = confirmation.getUser();
                    if (user != null && !user.isActive()) {
                        final List<GameTip> gameTips = user.getGameTips();
                        final List<ExtraTip> extraTips = user.getExtraTips();
                        if ( ((gameTips == null) || (gameTips.size() <= 0)) && ((extraTips == null) || (extraTips.size() <= 0)) ) {
                            LOG.info("Deleting user: '" + user.getUsername() + " (" + user.getEmail() + ")' - User did not activate within 2 days after registration and has no game tips and no extra tips.");
                            dataService.delete(user);
                        }
                    }
                }
                LOG.info("Finished Job: CleanupJob");
            }
        }
    }
}