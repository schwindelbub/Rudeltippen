package jobs;

import java.util.List;

import models.AbstractJob;
import models.Game;
import models.User;
import models.enums.Constants;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;
import services.MailService;
import services.ResultService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class GameTipJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GameTipJob.class);

    @Inject
    private DataService dataService;

    @Inject
    private MailService mailService;

    @Inject
    private ResultService resultService;

    public GameTipJob() {
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (resultService.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName(Constants.GAMETIPJOB.value());
            if (job != null && job.isActive()) {
                LOG.info("Started Job: " + Constants.GAMETIPJOB.value());
                final List<User> users = dataService.findAllNotifiableUsers();
                final List<Game> games = dataService.findAllNotifiableGames();

                if (games != null && games.size() > 0) {
                    for (final User user : users) {
                        mailService.gametips(user, games);
                    }

                    for (final Game game : games) {
                        game.setInformed(true);
                        dataService.save(game);
                    }
                }

                LOG.info("Finished Job: " + Constants.GAMETIPJOB.value());
            }
        }
    }
}