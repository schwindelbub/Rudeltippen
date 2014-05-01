package controllers;

import java.util.List;

import ninja.Result;
import models.Playday;
import models.Settings;
import models.User;
import models.statistic.GameTipStatistic;
import utils.AppUtils;
import utils.DataUtils;

public class ApplicationController {

    public Result index() {
        final int pointsDiff = AppUtils.getPointsToFirstPlace();
        final String diffToTop = AppUtils.getDiffToTop(pointsDiff);
        final Playday playday = AppUtils.getCurrentPlayday();
        final List<User> topUsers = User.find("SELECT u FROM User u WHERE active = true ORDER BY place ASC").fetch(3);
        final long users = AppUtils.getAllActiveUsers().size();

        render(topUsers, playday, users, diffToTop);
    }

    public Result rules() {
        final Settings settings = AppUtils.getSettings();
        render(settings);
    }

    public Result statistics() {
        final List<Object[]> games = DataUtils.getGameStatistics();
        final List<Object[]> results = DataUtils.getResultsStatistic();
        final List<GameTipStatistic> gameTipStatistics = GameTipStatistic.find("ORDER BY playday ASC").fetch();

        render(results, gameTipStatistics, games);
    }
}