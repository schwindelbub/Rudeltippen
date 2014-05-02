package jobs;

import java.util.List;

import models.AbstractJob;
import models.Game;
import models.WSResults;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;
import services.ResultService;

import com.google.inject.Inject;

public class ResultsJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(GameTipJob.class);

    @Inject
    private DataService dataService;

    @Inject
    private ResultService resultService;

    public ResultsJob() {
        //TODO Refactoring
        //        this.setDescription(Messages.get("job.resultsjob.descrption"));
        //        this.setExecuted(Messages.get("job.resultsjob.executed"));
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (dataService.isJobInstance()) {
            AbstractJob job = dataService.findAbstractJobByName("ResultsJob");
            if (job != null && job.isActive()) {
                LOG.info("Started Job: ResultsJob");
                final List<Game> games = dataService.findAllGamesWithNoResult();
                for (final Game game : games) {
                    final WSResults wsResults = resultService.getResultsFromWebService(game);
                    if ((wsResults != null) && wsResults.isUpdated()) {
                        dataService.setGameScoreFromWebService(game, wsResults);
                    }
                }
                LOG.info("Finished Job: ResultsJob");
            }
        }
    }
}