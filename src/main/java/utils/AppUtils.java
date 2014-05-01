package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Bracket;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
import models.Playday;
import models.Settings;
import models.Team;
import models.User;
import models.WSResult;
import models.WSResults;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import services.NotificationService;
import services.StatisticService;
import services.ValidationService;

public class AppUtils {
    /**
     * Loads the currents settings from database
     * @return Settings object
     */
    public static Settings getSettings() {
        return Settings.find("byAppName", APPNAME).first();
    }

    /**
     * Hashes a given clear-text password with a given salt using 100000 rounds
     *
     * @param userpass The password
     * @param usersalt The salt
     * @return SHA1 hashed string
     */
    public static String hashPassword(final String userpass, final String usersalt) {
        final String salt = AppUtils.getSettings().getAppSalt();
        String hash = "";
        for (int i = 1; i <= 100000; i++) {
            hash = DigestUtils.sha512Hex(hash + salt + userpass + usersalt);
        }

        return hash;
    }

    /**
     * Calculates the difference points between the logged in player and the first place
     * 
     * @return The difference
     */
    public static int getPointsToFirstPlace() {
        final User connectedUser = AppUtils.getConnectedUser();
        final User user = User.find("byPlace", 1).first();

        int pointsDiff = 0;
        if (user != null && connectedUser != null) {
            pointsDiff = user.getPoints() - connectedUser.getPoints();
        }

        return pointsDiff;
    }

    /**
     * Checks if at least one extra tip in given list is tipable
     *
     * @param extras List of extra tips
     * @return true if at least one extra tip is tipable, false otherwise
     */
    public static boolean extrasTipable(final List<Extra> extras) {
        boolean tippable = false;
        for (final Extra extra : extras) {
            if (extra.isTipable()) {
                tippable = true;
                break;
            }
        }

        return tippable;
    }

    /**
     * Sets the default application language defined in application.conf
     *
     * Default: en
     */
    public static void setAppLanguage() {
        String lang = Play.configuration.getProperty("default.language");
        if (StringUtils.isBlank(lang)) {
            lang = "en";
        }
        Lang.change(lang);
    }

    /**
     * Finds the username in session and loads the user from the database
     *
     * @return User object, null if not user is found
     */
    public static User getConnectedUser() {
        final String username = Security.connected();
        User connectedUser = null;
        if (StringUtils.isNotBlank(username)) {
            connectedUser = User.find("SELECT u FROM User u WHERE active = true AND username = ? OR email = ?", username, username).first();
        }

        return connectedUser;
    }

    /**
     * Checks if the current application matches the defined job instance name
     *
     * @return true if current instance is job instance, false otherwise
     */
    public static boolean isJobInstance() {
        boolean isInstance = false;
        final String appName = Play.configuration.getProperty("application.name");
        final String jobInstance = Play.configuration.getProperty("app.jobinstance");
        if (StringUtils.isNotBlank(appName) && StringUtils.isNotBlank(jobInstance) && appName.equalsIgnoreCase(jobInstance)) {
            isInstance = true;
        }

        return isInstance;
    }

    /**
     * All calculations are called from this method
     * - Calculation of brackets
     * - Calculation of team places
     * - Calculation of user points
     * - Calculation of user places
     * - Calculation of playoff teams
     * - Calculation of current playdays
     */
    public static void calculations() {
        calculateBrackets();
        setTeamPlaces();
        calculateUserPoints();
        setUserPlaces();
        setPlayoffTeams();

        if (setCurrentPlayday()) {
            flushAndClear();
            calculateStatistics();
        }
    }

    /**
     * Calculates the statistics for all playdays
     */
    private static void calculateStatistics() {
        final List<Playday> playdays = Playday.find("SELECT p FROM Playday p ORDER BY number ASC").fetch();
        final List<User> users = AppUtils.getAllActiveUsers();
        for (final Playday playday : playdays) {
            if (playday.allGamesEnded()) {
                final Map<String, Integer> scores = StatisticService.getScores(playday);
                StatisticService.setPlaydayStatistics(playday, scores);

                for (final User user : users) {
                    StatisticService.setPlaydayPoints(playday, user);
                    StatisticService.setAscendingPlaydayPoints(playday, user);
                }

                StatisticService.setPlaydayPlaces(playday);
                StatisticService.setGameTipStatistics(playday);
                StatisticService.setGameStatistic(playday);
            }
        }

        for (final User user : users) {
            StatisticService.setResultStatistic(user);
        }
    }

    /**
     * Calculation the points for each user based on game and extra tips
     */
    private static void calculateUserPoints() {
        final Settings settings = AppUtils.getSettings();

        final List<Extra> extras = Extra.findAll();
        for (final Extra extra : extras) {
            if (extra.getAnswer() == null) {
                if (allReferencedGamesEnded(extra.getGameReferences())) {
                    final Team team = AppUtils.getTeamByReference(extra.getExtraReference());
                    if (team != null) {
                        extra.setAnswer(team);
                        extra._save();
                    }
                }
            }
        }

        final List<User> users = getAllActiveUsers();
        for (final User user : users) {
            int correctResults = 0;
            int correctDifferences = 0;
            int correctTrends = 0;
            int correctExtraTips = 0;
            int userTipPoints = 0;

            final List<GameTip> gameTips = GameTip.find("byUser", user).fetch();
            for (final GameTip gameTip : gameTips) {
                final Game game = gameTip.getGame();

                if (!game.isEnded()) {
                    continue;
                }

                int pointsForTipp = 0;
                if (game.isOvertime()) {
                    pointsForTipp = getTipPointsOvertime(Integer.parseInt(game.getHomeScore()), Integer.parseInt(game.getAwayScore()), Integer.parseInt(game.getHomeScoreOT()), Integer.parseInt(game.getAwayScoreOT()), gameTip.getHomeScore(), gameTip.getAwayScore());
                } else {
                    pointsForTipp = getTipPoints(Integer.parseInt(game.getHomeScore()), Integer.parseInt(game.getAwayScore()), gameTip.getHomeScore(), gameTip.getAwayScore());
                }
                gameTip.setPoints(pointsForTipp);
                gameTip._save();

                if (pointsForTipp == settings.getPointsTip()) {
                    correctResults++;
                } else if (pointsForTipp == settings.getPointsTipDiff()) {
                    correctDifferences++;
                } else if (pointsForTipp == settings.getPointsTipTrend()) {
                    correctTrends++;
                }

                userTipPoints = userTipPoints + pointsForTipp;
            }
            user.setTipPoints(userTipPoints);
            user.setCorrectResults(correctResults);
            user.setCorrectDifferences(correctDifferences);
            user.setCorrectTrends(correctTrends);

            int bonusPoints = 0;
            for (final Extra extra : extras) {
                final ExtraTip extraTip = ExtraTip.find("byUserAndExtra", user, extra).first();
                if (extraTip != null) {
                    final Team bonusAnswer = extra.getAnswer();
                    final Team userAnswer = extraTip.getAnswer();
                    if ((bonusAnswer != null) && (userAnswer != null) && bonusAnswer.equals(userAnswer)) {
                        final int bPoints = extra.getPoints();
                        extraTip.setPoints(bPoints);
                        correctExtraTips++;
                        extraTip._save();
                        bonusPoints = bonusPoints + bPoints;
                    }
                }
            }

            user.setExtraPoints(bonusPoints);
            user.setPoints(bonusPoints + userTipPoints);
            user.setCorrectExtraTips(correctExtraTips);
            user._save();
        }
    }

    /**
     * Calculates the points, goals, etc. for each bracket
     */
    private static void calculateBrackets() {
        final Settings settings = AppUtils.getSettings();
        final int pointsWin = settings.getPointsGameWin();
        final int pointsDraw = settings.getPointsGameDraw();

        final List<Team> teams = Team.findAll();
        for (final Team team : teams) {
            final List<Game> homeGames = Game.find("byHomeTeam", team).fetch();
            final List<Game> awayGames = Game.find("byAwayTeam", team).fetch();

            int homePoints = 0;
            int awayPoints = 0;
            int gamesPlayed = 0;
            int gamesWon = 0;
            int gamesDraw = 0;
            int gamesLost = 0;
            int goalsFor = 0;
            int goalsAgainst = 0;
            for (final Game game : homeGames) {
                if (!game.isPlayoff()) {
                    if (ValidationService.isValidScore(game.getHomeScore(), game.getAwayScore())) {
                        final int points = game.getHomePoints();
                        homePoints = homePoints + points;
                        gamesPlayed++;

                        if (points == pointsWin) {
                            gamesWon++;
                        } else if (points == pointsDraw) {
                            gamesDraw++;
                        } else if (points == 0) {
                            gamesLost++;
                        }
                        goalsFor = goalsFor + Integer.parseInt(game.getHomeScore());
                        goalsAgainst = goalsAgainst + Integer.parseInt(game.getAwayScore());
                    }
                }
            }

            for (final Game game : awayGames) {
                if (!game.isPlayoff()) {
                    if (ValidationService.isValidScore(game.getHomeScore(), game.getAwayScore())) {
                        final int points = game.getAwayPoints();
                        awayPoints = awayPoints + points;
                        gamesPlayed++;

                        if (points == pointsWin) {
                            gamesWon++;
                        } else if (points == pointsDraw) {
                            gamesDraw++;
                        } else if (points == 0) {
                            gamesLost++;
                        }
                        goalsFor = goalsFor + Integer.parseInt(game.getAwayScore());
                        goalsAgainst = goalsAgainst + Integer.parseInt(game.getHomeScore());
                    }
                }
            }
            team.setPoints(homePoints + awayPoints);
            team.setGamesDraw(gamesDraw);
            team.setGamesLost(gamesLost);
            team.setGamesWon(gamesWon);
            team.setGamesPlayed(gamesPlayed);
            team.setGoalsFor(goalsFor);
            team.setGoalsAgainst(goalsAgainst);
            team.setGoalsDiff(goalsFor - goalsAgainst);
            team._save();
        }
    }

    /**
     * Sets the places of the user
     */
    private static void setUserPlaces() {
        int place = 1;
        final List<User> users = User.find("SELECT u FROM User u WHERE active = true ORDER BY points DESC, correctResults DESC, correctDifferences DESC, correctTrends DESC, correctExtraTips DESC").fetch();
        for (final User user : users) {
            user.setPreviousPlace(user.getPlace());
            user.setPlace(place);
            user._save();
            place++;
        }
    }

    /**
     * Sets the current playday based on all games played on a playday
     */
    private static boolean setCurrentPlayday() {
        boolean changed = false;
        final Playday currentPlayday = getCurrentPlayday();
        final List<Playday> playdays = Playday.find("SELECT p FROM Playday p ORDER BY number ASC").fetch();
        for (final Playday playday : playdays) {
            if (playday.allGamesEnded()) {
                playday.setCurrent(false);
                playday._save();
            } else {
                playday.setCurrent(true);
                playday._save();
                break;
            }
        }

        if (currentPlayday != getCurrentPlayday()) {
            changed = true;
            NotificationService.sendTopThree(currentPlayday);
        }

        return changed;
    }

    /**
     * Sets the teams to the playoff games
     */
    public static void setPlayoffTeams() {
        final Settings settings = AppUtils.getSettings();
        if (settings.isPlayoffs()) {
            Team homeTeam = null;
            Team awayTeam = null;

            final List<Bracket> brackets = Bracket.findAll();
            for (final Bracket bracket : brackets) {
                if (bracket.allGamesEnded()) {
                    final int number = bracket.getNumber();
                    final String bracketString = "B-" + number + "%";
                    final List<Game> games = Game.find("SELECT g FROM Game g WHERE homeReference LIKE ? OR awayReference LIKE ?", bracketString, bracketString).fetch();
                    for (final Game game : games) {
                        homeTeam = AppUtils.getTeamByReference(game.getHomeReference());
                        awayTeam = AppUtils.getTeamByReference(game.getAwayReference());
                        game.setHomeTeam(homeTeam);
                        game.setAwayTeam(awayTeam);
                        game._save();
                    }
                }
            }

            final List<Game> playoffGames = Game.find("byPlayoffAndEndedAndBracket", true, false, null).fetch();
            for (final Game game : playoffGames) {
                homeTeam = AppUtils.getTeamByReference(game.getHomeReference());
                awayTeam = AppUtils.getTeamByReference(game.getAwayReference());
                game.setHomeTeam(homeTeam);
                game.setAwayTeam(awayTeam);
                game._save();
            }
        }
    }

    /**
     * Sets the places of the teams in all brackets
     */
    private static void setTeamPlaces() {
        final List<Bracket> brackets = Bracket.find("byUpdateble", true).fetch();
        for (final Bracket bracket : brackets) {
            final List<Team> teams = Team.find("SELECT t FROM Team t WHERE bracket_id = ? ORDER BY points DESC, goalsDiff DESC, goalsFor DESC", bracket.getId()).fetch();
            int place = 1;
            for (final Team team : teams) {
                team.setPreviousPlace(team.getPlace());
                team.setPlace(place);
                team._save();
                place++;
            }
        }
    }

    /**
     * Sets the score of a game
     *
     * @param gameId The game id
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     * @param extratime The type of extratime if the game has extratime
     * @param homeScoreExtratime The score of the home time in extratime
     * @param awayScoreExtratime The score of the away time in extratime
     */
    public static void setGameScore(final String gameId, final String homeScore, final String awayScore, final String extratime, final String homeScoreExtratime, final String awayScoreExtratime) {
        if (ValidationService.isValidScore(homeScore, awayScore)) {
            final Game game = Game.findById(Long.parseLong(gameId));
            if (game != null) {
                saveScore(game, homeScore, awayScore, extratime, homeScoreExtratime, awayScoreExtratime);
            }
        }
    }

    /**
     * Checks if all games in given list have ended
     *
     * @param games The games to check
     * @return true if all games have ended, false otherweise
     */
    public static boolean allReferencedGamesEnded(final List<Game> games) {
        boolean ended = true;
        if ((games != null) && (games.size() > 0)) {
            for (final Game game : games) {
                if (!game.isEnded()) {
                    ended = false;
                    break;
                }
            }
        }

        return ended;
    }

    /**
     * Loads a team from the database from a reference string
     *
     * e.g.:
     * B-1-1  = Gets team from bracket 1 at place 1
     * G-12-W = Gets the winner team from game 12
     * G-12-L = Gets the looser team from game 12
     *
     * @param reference A string reference
     * @return The team object
     */
    public static Team getTeamByReference(final String reference) {
        Team team = null;
        if (StringUtils.isNotBlank(reference)) {
            final String[] references = reference.split("-");
            if ((references != null) && (references.length == 3)) {
                if ("B".equals(references[0])) {
                    final Bracket bracket = Bracket.find("byNumber", Integer.parseInt(references[1])).first();
                    if (bracket != null) {
                        team = bracket.getTeamByPlace(Integer.parseInt(references[2]));
                    }
                } else if ("G".equals(references[0])) {
                    final Game aGame = Game.find("byNumber", Integer.parseInt(references[1])).first();
                    if ((aGame != null) && aGame.isEnded()) {
                        if ("W".equals(references[2])) {
                            team = aGame.getWinner();
                        } else if ("L".equals(references[2])) {
                            team = aGame.getLoser();
                        }
                    }
                }
            }
        }

        return team;
    }

    /**
     * Returns the points for a given score and a given tip
     *
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     * @param homeScoreTipp The tip for the score of the home team
     * @param awayScoreTipp The tip for the score of the away team
     * @return
     */
    public static int getTipPoints(final int homeScore, final int awayScore, final int homeScoreTipp, final int awayScoreTipp) {
        final Settings settings = AppUtils.getSettings();
        int points = 0;

        if ((homeScore == homeScoreTipp) && (awayScore == awayScoreTipp)) {
            points = settings.getPointsTip();
        } else if ((homeScore - awayScore) == (homeScoreTipp - awayScoreTipp)) {
            points = settings.getPointsTipDiff();
        } else if ((awayScore - homeScore) == (awayScoreTipp - homeScoreTipp)) {
            points = settings.getPointsTipDiff();
        } else {
            points = getTipPointsTrend(homeScore, awayScore, homeScoreTipp, awayScoreTipp);
        }

        return points;
    }

    /**
     * Return the points for a trend if getTipPoints doenst find a previous match
     *
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     * @param homeScoreTipp The tip for the score of the home team
     * @param awayScoreTipp The tip for the score of the away team
     * @return
     */
    public static int getTipPointsTrend(final int homeScore, final int awayScore, final int homeScoreTipp, final int awayScoreTipp) {
        final Settings settings = AppUtils.getSettings();
        int points = 0;

        if ((homeScore > awayScore) && (homeScoreTipp > awayScoreTipp)) {
            points = settings.getPointsTipTrend();
        } else if ((homeScore < awayScore) && (homeScoreTipp < awayScoreTipp)) {
            points = settings.getPointsTipTrend();
        }

        return points;
    }

    /**
     * Return the points for a game in overtime
     *
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     * @param homeScoreOT The score of the home team after overtime
     * @param awayScoreOT The score of the away team after overtime
     * @param homeScoreTipp The tip for the score of the home team
     * @param awayScoreTipp The tip for the score of the away team
     * @return
     */
    public static int getTipPointsOvertime(final int homeScore, final int awayScore, final int homeScoreOT, final int awayScoreOT, final int homeScoreTipp, final int awayScoreTipp) {
        final Settings settings = AppUtils.getSettings();
        int points = 0;

        if (settings.isCountFinalResult()) {
            points = getTipPoints(homeScoreOT, awayScoreOT, homeScoreTipp, awayScoreTipp);
        } else {
            if ((homeScore == awayScore) && (homeScore == homeScoreTipp) && (awayScore == awayScoreTipp)) {
                points = settings.getPointsTip();
            } else if ((homeScore == awayScore) && (homeScoreTipp == awayScoreTipp)) {
                points = settings.getPointsTipDiff();
            }
        }

        return points;
    }

    /**
     * Saves a score to the database
     *
     * @param game The game object
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     * @param extratime The name of the extratime
     * @param homeScoreExtratime The score of the home team in extratime
     * @param awayScoreExtratime The score of the away team in extratime
     */
    private static void saveScore(final Game game, final String homeScore, final String awayScore, final String extratime, String homeScoreExtratime, String awayScoreExtratime) {
        final int[] points = AppUtils.getPoints(Integer.parseInt(homeScore), Integer.parseInt(awayScore));
        game.setHomePoints(points[0]);
        game.setAwayPoints(points[1]);
        game.setHomeScore(homeScore);
        game.setAwayScore(awayScore);
        if (ValidationService.isValidScore(homeScoreExtratime, awayScoreExtratime )) {
            homeScoreExtratime = homeScoreExtratime.trim();
            awayScoreExtratime = awayScoreExtratime.trim();
            game.setOvertimeType(extratime);
            game.setHomeScoreOT(homeScoreExtratime);
            game.setAwayScoreOT(awayScoreExtratime);
            game.setOvertime(true);
        } else {
            game.setOvertime(false);
        }

        if (!game.isEnded()) {
            NotificationService.sendNotfications(game);
            game.setEnded(true);
        }
        game._save();
    }

    /**
     * Calculates the points for the home and away team based on the score
     *
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     * @return Array containing the points for the home team [0] and the away team [1]
     */
    public static int[] getPoints(final int homeScore, final int awayScore) {
        final Settings settings = AppUtils.getSettings();
        final int[] points = new int[2];

        if (homeScore == awayScore) {
            points[0] = settings.getPointsGameDraw();
            points[1] = settings.getPointsGameDraw();
        } else if (homeScore > awayScore) {
            points[0] = settings.getPointsGameWin();
            points[1] = 0;
        } else if (homeScore < awayScore) {
            points[0] = 0;
            points[1] = settings.getPointsGameWin();
        }

        return points;
    }

    /**
     * Saves a tip for the currently connected user to the database
     *
     * @param game The game object
     * @param homeScore The score of the home team
     * @param awayScore The score of the away team
     */
    public static void placeTip(final Game game, final int homeScore, final int awayScore) {
        final User user = AppUtils.getConnectedUser();
        GameTip gameTip = GameTip.find("byUserAndGame", user, game).first();
        if (game.isTippable() && ValidationService.isValidScore(String.valueOf(homeScore), String.valueOf(awayScore))) {
            if (gameTip == null) {
                gameTip = new GameTip();
                gameTip.setGame(game);
                gameTip.setUser(user);
            }
            gameTip.setPlaced(new Date());
            gameTip.setHomeScore(homeScore);
            gameTip.setAwayScore(awayScore);
            gameTip._save();
            Logger.info("Tipp placed - " + user.getEmail() + " - " + gameTip);
        }
    }

    /**
     * Gathers the tips for a given playday for a given list of users
     *
     * @param playday The playday object
     * @param users List of users
     * @return All tips for the users and the playday
     */
    public static List<Map<User, List<GameTip>>> getPlaydayTips(final Playday playday, final List<User> users) {
        final List<Map<User, List<GameTip>>> tips = new ArrayList<Map<User, List<GameTip>>>();

        for (final User user : users) {
            final Map<User, List<GameTip>> userTips = new HashMap<User, List<GameTip>>();
            final List<GameTip> gameTips = new ArrayList<GameTip>();
            for (final Game game : playday.getGames()) {
                GameTip gameTip = GameTip.find("byGameAndUser", game, user).first();
                if (gameTip == null) {
                    gameTip = new GameTip();
                }
                gameTips.add(gameTip);
            }
            userTips.put(user,  gameTips);
            tips.add(userTips);
        }

        return tips;
    }

    /**
     * Gathers all extra tips for a given list of extra tips and a given user list
     *
     * @param users The list of user
     * @param extras The list of extra tips
     * @return All extra tips for the given users
     */
    public static List<Map<User, List<ExtraTip>>> getExtraTips(final List<User> users, final List<Extra> extras) {
        final List<Map<User, List<ExtraTip>>> tips = new ArrayList<Map<User, List<ExtraTip>>>();

        for (final User user : users) {
            final Map<User, List<ExtraTip>> userTips = new HashMap<User, List<ExtraTip>>();
            final List<ExtraTip> extraTips = new ArrayList<ExtraTip>();
            for (final Extra extra : extras) {
                ExtraTip extraTip = ExtraTip.find("byExtraAndUser", extra, user).first();
                if (extraTip == null) {
                    extraTip = new ExtraTip();
                }
                extraTips.add(extraTip);
            }
            userTips.put(user, extraTips);
            tips.add(userTips);
        }

        return tips;
    }

    /**
     * Returns the current Playday where current = true or
     * the playday with number 1 when none other found
     * @return Playday object
     */
    public static Playday getCurrentPlayday () {
        Playday playday = Playday.find("byCurrent", true).first();
        if (playday == null) {
            playday = Playday.find("byNumber", 1).first();
        }

        return playday;
    }

    /**
     * Parses a game from OpenLigaDB stores it in the database
     *
     * @param game The game object to store
     * @param wsResults WSResults object containing the data from the webservice
     */
    public static void setGameScoreFromWebService(final Game game, final WSResults wsResults) {
        final Map<String, WSResult> wsResult = wsResults.getWsResult();

        String homeScore = null;
        String awayScore = null;
        String homeScoreExtratime = null;
        String awayScoreExtratime = null;
        String extratime = null;

        if (wsResult.containsKey("90")) {
            homeScore = wsResult.get("90").getHomeScore();
            awayScore = wsResult.get("90").getAwayScore();
        }

        if (wsResult.containsKey("121")) {
            homeScoreExtratime = wsResult.get("121").getHomeScore();
            awayScoreExtratime = wsResult.get("121").getAwayScore();
            extratime = "ie";
        } else if (wsResult.containsKey("120")) {
            homeScoreExtratime = wsResult.get("120").getHomeScore();
            awayScoreExtratime = wsResult.get("120").getAwayScore();
            extratime = "nv";
        }

        Logger.info("Recieved from WebService - HomeScore: " + homeScore + " AwayScore: " + awayScore);
        Logger.info("Recieved from WebService - HomeScoreExtra: " + homeScoreExtratime + " AwayScoreExtra: " + awayScoreExtratime + " (" + extratime + ")");
        Logger.info("Updating results from WebService. " + game);
        setGameScore(String.valueOf(game.getId()), homeScore, awayScore, extratime, homeScoreExtratime, awayScoreExtratime);
        calculations();
    }

    /**
     * Returns a Base64 encoded Image from Gravatar if available
     * 
     * @param email The email adress to check
     * @param d Return a default image if no email is available
     * @return Base64 encoded Image, null if no image on gravatar exists
     */
    public static String getGravatarImage(final String email, final String d, int size) {
        String image = null;

        if (ValidationService.isValidEmail(email)) {
            HttpResponse response = null;
            String url = null;

            if ((size <= 0) || (size > PICTURELARGE)) {
                size = PICTURESMALL;
            }

            if (StringUtils.isNotBlank(d)) {
                url = "https://secure.gravatar.com/avatar/" + Codec.hexMD5(email) + ".jpg?s=" + size + "&r=pg&d=" + d;
            } else {
                url = "https://secure.gravatar.com/avatar/" + Codec.hexMD5(email) + ".jpg?s=" + size + "&r=pg";
            }

            response = WS.url(url).get();
            if ((response != null) && response.success()) {
                try {
                    final File file = new File(Codec.UUID());
                    final InputStream inputStream = response.getStream();
                    final OutputStream out = new FileOutputStream(file);
                    final byte buf[] = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    inputStream.close();

                    image = Images.toBase64(file);
                    file.delete();
                } catch (final Exception e) {
                    Logger.error("Failed to get and convert gravatar image. " + e);
                }
            }
        }

        return image;
    }

    /**
     * Stores an extratip in the database
     * 
     * @param extra The extra object
     * @param team The team which is the extra answer
     */
    public static void placeExtraTip(final Extra extra, final Team team) {
        final User user = AppUtils.getConnectedUser();
        if (team != null) {
            ExtraTip extraTip = ExtraTip.find("byUserAndExtra", user, extra).first();
            if (extraTip == null) {
                extraTip = new ExtraTip();
            }

            extraTip.setUser(user);
            extraTip.setExtra(extra);
            extraTip.setAnswer(team);
            extraTip._save();
            Logger.info("Stored extratip - " + user.getEmail() + " - " + extraTip);
        }
    }

    /**
     * Returns a Message with the difference to place 1
     * 
     * @param pointsDiff The difference in points
     * @return Message with difference or empty string
     */
    public static String getDiffToTop(final int pointsDiff) {
        String message = "";
        if (pointsDiff == 1) {
            message = Messages.get("points.to.top.one", pointsDiff);
        } else if (pointsDiff > 1) {
            message = Messages.get("points.to.top.many", pointsDiff);
        }

        return message;
    }

    /**
     * Returns the current timezone configured in application.conf or
     * the default timezone defined in IAppConstants
     * 
     * @return The String value of the timezone
     */
    public static String getCurrentTimeZone() {
        String timezone = Play.configuration.getProperty("app.timezone");
        if (StringUtils.isBlank(timezone)) {
            timezone = DEFAULT_TIMEZONE;
        }
        return timezone;
    }

    /**
     * Checks if rudeltippen is inizialized
     * 
     * @return True if initialized, false otherwise
     */
    public static boolean appIsInizialized() {
        return getSettings() != null ? true : false;
    }

    /**
     * Checks if the user with a given username and userpass
     * matches an entry in the database
     * 
     * @param username The username
     * @param userpass The hashed passowrd of the user
     * @return The User object, if found in database
     */
    public static User connectUser(final String username, final String userpass) {
        User user = User.find("byUsernameAndUserpassAndActive", username, userpass, true).first();
        if (user == null) {
            user = User.find("byEmailAndUserpassAndActive", username, userpass, true).first();
        }

        return user;
    }

    /**
     * Return a list with all active users
     * 
     * @return A List of all active Users
     */
    public static List<User> getAllActiveUsers() {
        List<User> users = User.find("byActive", true).fetch();
        if (users == null) {
            users = new ArrayList<User>();
        }

        return users;
    }

    /**
     * Sends a flush and clear command to the entitymanager
     */
    public static void flushAndClear() {
        JPA.em().flush();
        JPA.em().clear();
    }
}