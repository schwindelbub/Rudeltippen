package jobs;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import models.AbstractJob;
import models.Game;
import models.User;
import services.DataService;
import services.MailService;
import utils.AppUtils;

public class GameTipJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GameTipJob.class);

    @Inject
    private DataService dataService;

    @Inject
    private MailService mailService;

    public GameTipJob() {
        //TODO Refactoring
        //        this.setDescription(Messages.get("job.gametipjob.description"));
        //        this.setExecuted(Messages.get("job.gametipjob.executed"));
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (AppUtils.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName("GameTipJob");
            if (job != null && job.isActive()) {
                LOG.info("Started Job: GameTipJob");
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

                LOG.info("Finished Job: GameTipJob");
            }
        }
    }
}