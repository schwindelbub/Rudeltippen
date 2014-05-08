package utils;

import java.util.Date;
import java.util.List;

import models.Bracket;
import models.Game;
import models.GameTip;
import models.Team;
import models.User;
import services.I18nService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ViewUtils {
    
    @Inject
    private I18nService i18nService;
    
//        public static String difference (final Date date) {
//            final int MIN = 60;
//            final int HOUR = MIN * 60;
//            final int DAY = HOUR * 24;
//            final int MONTH = DAY * 30;
//            final int YEAR = DAY * 365;
//    
//            final Date now = new Date();
//            String difference = null;
//            if (date.after(now)) {
//                final long delta = (date.getTime() - now.getTime()) / 1000;
//                if (delta < 60) {
//                    difference = Messages.get("in.second" + pluralize(delta), delta);
//                } else if (delta < HOUR) {
//                    final long minutes = delta / MIN;
//                    difference = Messages.get("in.minute" + pluralize(minutes), minutes);
//                } else if (delta < DAY) {
//                    final long hours = delta / HOUR;
//                    difference = Messages.get("in.hour" + pluralize(hours), hours);
//                } else if (delta < MONTH) {
//                    final long days = delta / DAY;
//                    difference = Messages.get("in.day" + pluralize(days), days);
//                } else if (delta < YEAR) {
//                    final long months = delta / MONTH;
//                    difference = Messages.get("in.month" + pluralize(months), months);
//                } else {
//                    final long years = delta / YEAR;
//                    difference = Messages.get("in.year" + pluralize(years), years);
//                }
//            } else {
//                difference = Messages.get("in.ended");
//            }
//    
//            return difference;
//        }
//    
    public String homeReferenceName (final Game game) {
        return getReference(game.getHomeReference());
    }

    public String awayReferenceName (final Game game) {
        return getReference(game.getAwayReference());
    }
//    
//        public static String getGameTipAndPoints(final Game game) {
//            String tip = "-";
//            final User user = AppUtils.getConnectedUser();
//            final GameTip gameTip = GameTip.find("byGameAndUser", game, user).first();
//    
//            if (gameTip != null) {
//                if (gameTip.getGame() != null) {
//                    if (gameTip.getGame().isEnded()) {
//                        tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore() + " (" + gameTip.getPoints() + ")";
//                    } else {
//                        tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore();
//                    }
//                }
//            }
//    
//            return tip;
//        }
//    
//        public static String getHomeScoreTip(final Game game) {
//            String homeScore = "";
//            final User user = AppUtils.getConnectedUser();
//            final List<GameTip> gameTips = game.getGameTips();
//    
//            for (final GameTip gameTip : gameTips) {
//                if (gameTip.getUser().equals(user)) {
//                    homeScore = String.valueOf(gameTip.getHomeScore());
//                    break;
//                }
//            }
//    
//            return homeScore;
//        }
//    
//        public static String getAwayScoreTip(final Game game) {
//            String awayScore = "";
//            final User user = AppUtils.getConnectedUser();
//            final List<GameTip> gameTips = game.getGameTips();
//    
//            for (final GameTip gameTip : gameTips) {
//                if (gameTip.getUser().equals(user)) {
//                    awayScore = String.valueOf(gameTip.getAwayScore());
//                    break;
//                }
//            }
//    
//            return awayScore;
//        }
//    
//        public static String getPoints(final Game game) {
//            String points = "-";
//            final User user = AppUtils.getConnectedUser();
//            final List<GameTip> gameTips = game.getGameTips();
//    
//            for (final GameTip gameTip : gameTips) {
//                if (gameTip.getGame().isEnded() && gameTip.getUser().equals(user)) {
//                    points = String.valueOf(gameTip.getPoints());
//                    break;
//                }
//            }
//    
//            return points;
//        }
//    
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

    private String getReference(final String reference) {
        final String [] references = reference.split("-");
        String message = "";
        if (("G").equals(references[0])) {
            if ("W".equals(references[2])) {
                message = i18nService.get("model.game.winnergame") + " " + references[1];
            } else if (("L").equals(references[2])) {
                message = i18nService.get("model.game.losergame") + " " + references[1];
            }
        } else if (("B").equals(references[0])) {
            //TODO Refactoring
            final Bracket bracket = null;//Bracket.find("byNumber", Integer.parseInt(references[1])).first();
            final String groupName = "";//bracket.getName();
            final String placeName = getPlaceName(Integer.parseInt(references[2]));

            message = placeName + " " + i18nService.get(groupName);
        }

        return message;
    }

    public String getPlaceName(final int place) {
        String message = "";

        if (place == 1) {
            message = i18nService.get("helper.first");
        } else if (place == 2){
            message = i18nService.get("helper.second");
        } else if (place == 3){
            message = i18nService.get("helper.third");
        } else if (place == 4){
            message = i18nService.get("helper.fourth");
        } else if (place == 5){
            message = i18nService.get("helper.fifth");
        } else if (place == 6){
            message = i18nService.get("helper.six");
        } else if (place == 7){
            message = i18nService.get("helper.seventh");
        } else if (place == 8){
            message = i18nService.get("helper.eight");
        } else if (place == 9){
            message = i18nService.get("helper.ninth");
        } else if (place == 10){
            message = i18nService.get("helper.tenth");
        }

        return message;
    }

    public String getResult(final Game game) {
        String result = "-";
        if (game.isEnded()) {
            if (game.isOvertime()) {
                //TODO Refactoring
                result = game.getHomeScoreOT() + " : " + game.getAwayScoreOT() + " (OVERTIME Translation)";
            } else {
                result = game.getHomeScore() + " : " + game.getAwayScore();
            }
        }

        return result;
    }
//    
//        public static String getGameTipAndPoints(final GameTip gameTip) {
//            String tip = "-";
//            final Date date = new Date();
//    
//            final User user = AppUtils.getConnectedUser();
//            if (gameTip != null) {
//                final Game game = gameTip.getGame();
//                if (game != null) {
//                    if (game.isEnded()) {
//                        tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore() + " (" + gameTip.getPoints() + ")";
//                    } else {
//                        if (date.after(game.getTippEnding())) {
//                            tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore();
//                        } else {
//                            if (user.equals(gameTip.getUser())) {
//                                tip = gameTip.getHomeScore() + " : " + gameTip.getAwayScore();
//                            }
//                        }
//                    }
//                }
//            }
//    
//            return tip;
//        }
//    
//        public static long getExtraTip(final Extra extra) {
//            final User user = AppUtils.getConnectedUser();
//            final ExtraTip extraTip = ExtraTip.find("byExtraAndUser", extra, user).first();
//            long id = 0;
//    
//            if ((extraTip != null) && (extraTip.getAnswer() != null)) {
//                id = extraTip.getAnswer().getId();
//            }
//    
//            return id;
//        }
//    
//        public static String getAnswer(final Extra extra) {
//            final User user = AppUtils.getConnectedUser();
//            final ExtraTip extraTip = ExtraTip.find("byExtraAndUser", extra, user).first();
//            String answer = "";
//    
//            if ((extraTip != null) && (extraTip.getAnswer() != null)) {
//                answer = Messages.get(extraTip.getAnswer().getName());
//            }
//    
//            return answer;
//        }
//    
//        public static String getExtraTipAnswer(final ExtraTip extraTip) {
//            String answer = "-";
//            if (extraTip.getAnswer() != null) {
//                if (extraTip.getExtra().getEnding().getTime() < new Date().getTime()) {
//                    answer = Messages.get(extraTip.getAnswer().getName());
//                } else {
//                    answer = Messages.get("model.user.tipped");
//                }
//            }
//    
//            return answer;
//        }
//    
//        public static String getExtraTipPoints(final ExtraTip extraTip) {
//            String points = "";
//            if ((extraTip != null) && (extraTip.getExtra() != null) && (extraTip.getExtra().getAnswer() != null)) {
//                points = " ("+ extraTip.getPoints() + ")";
//            }
//    
//            return points;
//        }
//    
//        public static String htmlUnescape(final String html) {
//            return StringEscapeUtils.unescapeHtml(html);
//        }
//    
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
    
    //TODO Refactoring
    public Date getTippEnding(Game game) {
        final long time = game.getKickoff().getTime();
        //final int offset = AppUtils.findSettings().getMinutesBeforeTip() * 60000 ;
        //
        //        return new Date (time - offset);
        return null;
    }
}