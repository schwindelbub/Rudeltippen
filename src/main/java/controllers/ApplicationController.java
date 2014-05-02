package controllers;

import java.util.List;

import models.Playday;
import models.Settings;
import models.User;
import models.statistic.GameTipStatistic;
import ninja.Result;
import ninja.Results;
import services.DataService;
import services.I18nService;
import services.StatisticService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ApplicationController {

    @Inject
    private DataService dataService;

    @Inject
    private StatisticService statisticService;

    @Inject
    private I18nService i18nService;

    public Result index() {
        final int pointsDiff = dataService.getPointsToFirstPlace();
        final String diffToTop = i18nService.getDiffToTop(pointsDiff);
        final Playday playday = dataService.getCurrentPlayday();
        final List<User> topUsers = dataService.findTopThreeUsers();
        final long users = dataService.findAllActiveUsers().size();

        return Results.html().render(topUsers).render(playday).render(users).render(diffToTop);
    }

    public Result rules() {
        Settings settings = dataService.findSettings();
        return Results.html().render(settings);
    }

    public Result statistics() {
        final List<Object[]> games = statisticService.getGameStatistics();
        final List<Object[]> results = statisticService.getResultsStatistic();
        final List<GameTipStatistic> gameTipStatistics = statisticService.getGamTipStatisticsOrderByPlayday();

        return Results.html().render(results).render(gameTipStatistics).render(games);
    }
}