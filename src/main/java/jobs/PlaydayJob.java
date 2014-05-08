package jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import models.AbstractJob;
import models.Game;
import models.Playday;

import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import services.DataService;
import services.I18nService;
import services.ResultService;
import services.SetupService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class PlaydayJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GameTipJob.class);

    @Inject
    private DataService dataService;

    @Inject
    private SetupService setupService;
    
    @Inject
    private ResultService resultService;

    @Inject
    private I18nService i18nService;

    public PlaydayJob() {
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (resultService.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName("GameTipJob");
            if (job != null && job.isActive()) {
                LOG.info("Started Job: PlaydayJob");
                int number = dataService.findCurrentPlayday().getNumber();
                for (int i=0; i <= 3; i++) {
                    final Playday playday = dataService.findPlaydaybByNumber(number);
                    if (playday != null) {
                        final List<Game> games = playday.getGames();
                        for (final Game game : games) {
                            final String matchID = game.getWebserviceID();
                            if (StringUtils.isNotBlank(matchID) && game.isUpdateble()) {
                                final Document document = resultService.getDocumentFromWebService(matchID);
                                final Date kickoff = setupService.getKickoffFromDocument(document);
                                final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                df.setTimeZone(TimeZone.getTimeZone(i18nService.getCurrentTimeZone()));

                                game.setKickoff(kickoff);
                                dataService.save(game);

                                LOG.info("Updated Kickoff and MatchID of Playday: " + playday.getName());
                            }
                        }
                    }
                    number++;
                }
                LOG.info("Finished Job: PlaydayJob");
            }
        }
    }
}