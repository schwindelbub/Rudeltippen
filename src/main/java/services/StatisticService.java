package services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Inject;

import utils.AppUtils;
import models.Game;
import models.GameTip;
import models.Playday;
import models.Settings;
import models.User;
import models.statistic.GameStatistic;
import models.statistic.GameTipStatistic;
import models.statistic.PlaydayStatistic;
import models.statistic.ResultStatistic;
import models.statistic.UserStatistic;

public class StatisticService {

    @Inject
    private DataService dataService;

    public void setResultStatistic(final User user) {
        //TODO Refactoring
        //ResultStatistic.delete("user = ?", user);

        final Settings settings = AppUtils.getSettings();
        final List<GameTip> gameTips = dataService.findGameTipByUser(user);
        for (final GameTip gameTip : gameTips) {
            final Game game = gameTip.getGame();
            if ((game != null) && game.isEnded()) {
                final String score = gameTip.getHomeScore() + ":" + gameTip.getAwayScore();
                ResultStatistic resultStatistic = dataService.findResultStatisticByUserAndResult(user, score);
                if (resultStatistic == null) {
                    resultStatistic = new ResultStatistic();
                    resultStatistic.setUser(user);
                    resultStatistic.setResult(score);
                }

                final int points = gameTip.getPoints();
                if (points == settings.getPointsTip()) {
                    resultStatistic.setCorrectTips( resultStatistic.getCorrectTips() + 1 );
                } else if (points == settings.getPointsTipDiff()) {
                    resultStatistic.setCorrectDiffs( resultStatistic.getCorrectDiffs() + 1 );
                } else if (points == settings.getPointsTipTrend()) {
                    resultStatistic.setCorrectTrends( resultStatistic.getCorrectTrends() + 1 );
                }

                dataService.save(resultStatistic);
            }
        }
    }

    public void setGameStatistic(final Playday playday) {
        final Map<String, Integer> scores = new HashMap<String, Integer>();
        final List<Game> games = playday.getGames();
        for (final Game game : games) {
            if ((game != null) && game.isEnded()) {
                final String score = game.getHomeScore() + ":" + game.getAwayScore();
                if (!scores.containsKey(score)) {
                    scores.put(score, 1);
                } else {
                    scores.put(score, scores.get(score) + 1);
                }
            }
        }

        for (final Entry entry : scores.entrySet()) {
            GameStatistic gameStatistic = dataService.findGameStatisticByPlaydayAndResult(playday, entry.getKey());
            if (gameStatistic == null) {
                gameStatistic = new GameStatistic();
                gameStatistic.setPlayday(playday);
            }

            gameStatistic.setGameResult((String) entry.getKey());
            gameStatistic.setResultCount((Integer) entry.getValue());
            dataService.save(gameStatistic);
        }
    }

    public void setGameTipStatistics(final Playday playday) {
        GameTipStatistic gameTipStatistic = dataService.findGameTipStatisticByPlayday();
        if (gameTipStatistic == null) {
            gameTipStatistic = new GameTipStatistic();
            gameTipStatistic.setPlayday(playday);
        }

        final Object [] statistics = dataService.getPlaydayStatistics(playday);
        if ((statistics != null) && (statistics.length == 5)) {
            gameTipStatistic.setPoints(((Long) statistics [0]).intValue());
            gameTipStatistic.setCorrectTips(((Long) statistics [1]).intValue());
            gameTipStatistic.setCorrectDiffs(((Long) statistics [2]).intValue());
            gameTipStatistic.setCorrectTrends(((Long) statistics [3]).intValue());
            gameTipStatistic.setAvgPoints(((Double) statistics [4]).intValue());
        }

        dataService.save(gameTipStatistic);
    }

    public void setAscendingPlaydayPoints(final Playday playday, final User user) {
        final UserStatistic userStatistic = dataService.findUserStatisticByPlaydayAndUser(playday, user);

        final Object [] statistics = dataService.getAscendingStatistics(playday, user);
        if ((statistics != null) && (statistics.length == 4)) {
            userStatistic.setPoints(((Long) statistics [0]).intValue());
            userStatistic.setCorrectTips(((Long) statistics [1]).intValue());
            userStatistic.setCorrectDiffs(((Long) statistics [2]).intValue());
            userStatistic.setCorrectTrends(((Long) statistics [3]).intValue());
        }
        dataService.save(userStatistic);
    }

    public void setPlaydayPlaces(final Playday playday) {
        List<UserStatistic> userStatistics = dataService.findUserStatisticByPlaydayOrderByPlaydayPoints(playday);
        int place = 1;
        for (final UserStatistic userStatistic : userStatistics) {
            userStatistic.setPlaydayPlace(place);
            dataService.save(userStatistic);
            place++;
        }

        userStatistics = dataService.findUserStatisticByPlaydayOrderByPoints(playday);
        place = 1;
        for (final UserStatistic userStatistic : userStatistics) {
            userStatistic.setPlace(place);
            dataService.save(userStatistic);
            place++;
        }
    }

    public void setPlaydayPoints(final Playday playday, final User user) {
        int playdayPoints = 0;
        int correctTips = 0;
        int correctDiffs = 0;
        int correctTrends = 0;

        final Settings settings = AppUtils.getSettings();
        final List<Game> games = playday.getGames();
        for (final Game game : games) {
            final GameTip gameTip = dataService.findGameTipByGameAndUser(user, game);
            if (gameTip != null) {
                final int points = gameTip.getPoints();

                if (points == settings.getPointsTip()) {
                    correctTips++;
                } else if (points == settings.getPointsTipDiff()) {
                    correctDiffs++;
                } else if (points == settings.getPointsTipTrend()) {
                    correctTrends++;
                }
                playdayPoints = playdayPoints + points;
            }
        }

        UserStatistic userStatistic = dataService.findUserStatisticByPlaydayAndUser(playday, user);
        if (userStatistic == null) {
            userStatistic = new UserStatistic();
            userStatistic.setPlayday(playday);
            userStatistic.setUser(user);
        }
        userStatistic.setPlaydayPoints(playdayPoints);
        userStatistic.setPlaydayCorrectTips(correctTips);
        userStatistic.setPlaydayCorrectDiffs(correctDiffs);
        userStatistic.setPlaydayCorrectTrends(correctTrends);
        dataService.save(userStatistic);
    }

    public Map<String, Integer> getScores(final Playday playday) {
        final Map<String, Integer> scores = new HashMap<String, Integer>();

        final List<Game> games = playday.getGames();
        for (final Game game : games) {
            final List<GameTip> gameTips = dataService.findGameTipByGame(game);
            for (final GameTip gameTip : gameTips) {
                final String score = gameTip.getHomeScore() + ":" + gameTip.getAwayScore();
                if (!scores.containsKey(score)) {
                    scores.put(score, 1);
                } else {
                    scores.put(score, scores.get(score) + 1);
                }
            }
        }
        return scores;
    }

    public void setPlaydayStatistics(final Playday playday, final Map<String, Integer> scores) {
        for (final Entry entry : scores.entrySet()) {
            PlaydayStatistic playdayStatistic = dataService.findPlaydayStatisticByPlaydayAndResult(playday, entry.getKey());
            if (playdayStatistic == null) {
                playdayStatistic = new PlaydayStatistic();
                playdayStatistic.setPlayday(playday);
            }
            playdayStatistic.setGameResult((String) entry.getKey());
            playdayStatistic.setResultCount((Integer) entry.getValue());
            dataService.save(playdayStatistic);
        }
    }
}