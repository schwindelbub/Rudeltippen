package jobs;

import java.util.List;

import models.AbstractJob;
import models.Game;
import models.enums.Constants;
import models.ws.WSResults;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.CalculationService;
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
public class ResultJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GameTipJob.class);

    @Inject
    private DataService dataService;

    @Inject
    private CalculationService calculationService;

    @Inject
    private ResultService resultService;

    public ResultJob() {
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (resultService.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName(Constants.RESULTJOB.value());
            if (job != null && job.isActive()) {
                LOG.info("Started Job: " + Constants.RESULTJOB.value());
                final List<Game> games = dataService.findAllGamesWithNoResult();
                for (final Game game : games) {
                    final WSResults wsResults = resultService.getResultsFromWebService(game);
                    if ((wsResults != null) && wsResults.isUpdated()) {
                        calculationService.setGameScoreFromWebService(game, wsResults);
                    }
                }
                LOG.info("Finished Job: " + Constants.RESULTJOB.value());
            }
        }
    }
}