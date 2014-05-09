package services;

import java.util.Date;
import java.util.List;

import models.Game;
import models.GameTip;
import models.Team;
import models.User;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ViewService {
    
    @Inject
    private DataService dataService;
    
    @Inject
    private I18nService i18nService;
    
    public String getScore(final Game game) {
        String score = "- : -";
        if (game.isEnded()) {
            if (game.isOvertime()) {
                score = game.getHomeScore() + " : " + game.getAwayScore() + " / " + game.getHomeScoreOT() + ":" + game.getAwayScoreOT() + " (" + game.getOvertimeType() + ")";
            } else {
                score = game.getHomeScore() + " : " + game.getAwayScore();
            }
        }

        return score;
    }

    public String getGameTipAndPoints(final Game game, User user) {
        String tip = "-";
        final GameTip gameTip = dataService.findGameTipByGameAndUser(user, game);

        if (gameTip != null) {
            if (gameTip.getGame() != null) {
                if (gameTip.getGame().isEnded()) {
                    tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore() + " (" + gameTip.getPoints() + ")";
                } else {
                    tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore();
                }
            }
        }

        return tip;
    }

    public String getResult(final Game game) {
        String result = "-";
        if (game.isEnded()) {
            if (game.isOvertime()) {
                result = game.getHomeScoreOT() + " : " + game.getAwayScoreOT() + " (" + i18nService.get(game.getOvertimeType()) + ")";
            } else {
                result = game.getHomeScore() + " : " + game.getAwayScore();
            }
        }

        return result;
    }

    public String getTrend(final Game game) {
        String trend = i18nService.get("model.game.notenoughtipps");
        final List<GameTip> gameTips = game.getGameTips();
        if ((gameTips != null) && (gameTips.size() >= 4)) {
            int tipsHome = 0;
            int tipsDraw = 0;
            int tipsAway = 0;

            for (final GameTip gameTip : gameTips) {
                final int homeScore = gameTip.getHomeScore();
                final int awayScore = gameTip.getAwayScore();

                if (homeScore == awayScore) {
                    tipsDraw++;
                } else if (homeScore > awayScore) {
                    tipsHome++;
                } else if (homeScore < awayScore) {
                    tipsAway++;
                }
            }

            trend = tipsHome + " / " + tipsDraw + " / " + tipsAway;
        }

        return trend;
    }

    public String difference (final Date date) {
        final int MIN = 60;
        final int HOUR = MIN * 60;
        final int DAY = HOUR * 24;
        final int MONTH = DAY * 30;
        final int YEAR = DAY * 365;

        final Date now = new Date();
        String difference = null;
        if (date.after(now)) {
            final long delta = (date.getTime() - now.getTime()) / 1000;
            if (delta < 60) {
                difference = i18nService.get("in.second", new Object[]{delta});
            } else if (delta < HOUR) {
                final long minutes = delta / MIN;
                difference = i18nService.get("in.minute", new Object[]{minutes});
            } else if (delta < DAY) {
                final long hours = delta / HOUR;
                difference = i18nService.get("in.hour", new Object[]{hours});
            } else if (delta < MONTH) {
                final long days = delta / DAY;
                difference = i18nService.get("in.day", new Object[]{days});
            } else if (delta < YEAR) {
                final long months = delta / MONTH;
                difference = i18nService.get("in.month", new Object[]{months});
            } else {
                final long years = delta / YEAR;
                difference = i18nService.get("in.year", new Object[]{years});
            }
        } else {
            difference = i18nService.get("in.ended");
        }

        return difference;
    }

    public String getHomeScoreTip(final Game game, User user) {
        String homeScore = "";
        final List<GameTip> gameTips = game.getGameTips();

        for (final GameTip gameTip : gameTips) {
            if (gameTip.getUser().equals(user)) {
                homeScore = String.valueOf(gameTip.getHomeScore());
                break;
            }
        }

        return homeScore;
    }

    public String getAwayScoreTip(final Game game, User user) {
        String awayScore = "";
        final List<GameTip> gameTips = game.getGameTips();

        for (final GameTip gameTip : gameTips) {
            if (gameTip.getUser().equals(user)) {
                awayScore = String.valueOf(gameTip.getAwayScore());
                break;
            }
        }

        return awayScore;
    }

    public String getGameTipAndPoints(final GameTip gameTip, User user) {
        String tip = "-";
        final Date date = new Date();

        if (gameTip != null) {
            final Game game = gameTip.getGame();
            if (game != null) {
                if (game.isEnded()) {
                    tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore() + " (" + gameTip.getPoints() + ")";
                } else {
                    if (date.after(getTippEnding(game))) {
                        tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore();
                    } else {
                        if (user.equals(gameTip.getUser())) {
                            tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore();
                        }
                    }
                }
            }
        }

        return tip;
    }
    
    private Date getTippEnding(Game game) {
        final long time = game.getKickoff().getTime();
        final int offset = dataService.findSettings().getMinutesBeforeTip() * 60000 ;
        
        return new Date (time - offset);
    }

    public String getPlaceTrend(final User user) {
        final int currentPlace = user.getPlace();
        final int previousPlace = user.getPreviousPlace();
        String trend = "";

        if (previousPlace > 0) {
            if (currentPlace < previousPlace) {
                trend = "<i class=\"icon-arrow-up icon-green\"></i>" + " (" + previousPlace + ")";
            } else if (currentPlace > previousPlace) {
                trend = "<i class=\"icon-arrow-down icon-red\"></i>" + " (" + previousPlace + ")";
            } else {
                trend = "<i class=\"icon-minus\"></i>" + " (" + previousPlace + ")";
            }
        }

        return trend;
    }

    public String getPlaceTrend(final Team team) {
        final int currentPlace = team.getPlace();
        final int previousPlace = team.getPreviousPlace();
        String trend = "";

        if (previousPlace > 0) {
            if (currentPlace < previousPlace) {
                trend = "<i class=\"icon-arrow-up icon-green\"></i>" + " (" + previousPlace + ")";
            } else if (currentPlace > previousPlace) {
                trend = "<i class=\"icon-arrow-down icon-red\"></i>" + " (" + previousPlace + ")";
            } else {
                trend = "<i class=\"icon-minus\"></i>" + " (" + previousPlace + ")";
            }
        }

        return trend;
    }
}