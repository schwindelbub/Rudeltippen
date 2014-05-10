package jobs;

import java.util.ArrayList;
import java.util.List;

import models.AbstractJob;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
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
public class ReminderJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GameTipJob.class);

    @Inject
    private DataService dataService;

    @Inject
    private MailService mailService;

    @Inject
    private ResultService resultService;

    public ReminderJob() {
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (resultService.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName(Constants.REMINDERJOB.value());
            if (job != null && job.isActive()) {
                LOG.info("Started Job: " + Constants.REMINDERJOB.value());
                final List<Extra> nextExtras = dataService.findAllExtrasEnding();
                final List<Game> nextGames = dataService.findAllGamesEnding();
                final List<User> users = dataService.findAllRemindableUsers();

                for (final User user : users) {
                    final List<Game> reminderGames = new ArrayList<Game>();
                    final List<Extra> reminderBonus = new ArrayList<Extra>();

                    for (final Game game : nextGames) {
                        final GameTip gameTip = dataService.findGameTipByGameAndUser(user, game);
                        if (gameTip == null) {
                            reminderGames.add(game);
                        }
                    }

                    for (final Extra extra : nextExtras) {
                        final ExtraTip extraTip = dataService.findExtraTipByExtraAndUser(extra, user);
                        if (extraTip == null) {
                            reminderBonus.add(extra);
                        }
                    }

                    if (!reminderGames.isEmpty() || !reminderBonus.isEmpty()) {
                        mailService.reminder(user, reminderGames, reminderBonus);
                        LOG.info("Reminder send to: " + user.getEmail());
                    }
                }

                for (final Game game : nextGames) {
                    game.setReminder(true);
                    dataService.save(game);
                }

                for (final Extra extra : nextExtras) {
                    extra.setReminder(true);
                    dataService.save(extra);
                }

                LOG.info("Finshed Job: " + Constants.REMINDERJOB.value());
            }
        }
    }
}