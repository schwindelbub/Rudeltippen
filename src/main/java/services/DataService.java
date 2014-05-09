package services;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import models.AbstractJob;
import models.Bracket;
import models.Confirmation;
import models.ConfirmationType;
import models.Constants;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
import models.Playday;
import models.Settings;
import models.Team;
import models.User;
import models.statistic.GameStatistic;
import models.statistic.GameTipStatistic;
import models.statistic.PlaydayStatistic;
import models.statistic.ResultStatistic;
import models.statistic.UserStatistic;
import ninja.cache.NinjaCache;

import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class DataService {
    private static final Logger LOG = LoggerFactory.getLogger(DataService.class);
    private static final String PACKAGE = "models";
    private static final String DB = "rudeltippen";
    private Datastore datastore;

    @Inject
    private NinjaCache ninjaCache;
    
    @Inject
    private ResultService resultService;
    
    @Inject
    private NotificationService notificationService;
    
    @Inject
    private ValidationService validationService;
    
    @Inject
    private CommonService commonService;

    public DataService() {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient();
        } catch (UnknownHostException e) {
            LOG.error("Failed to connect to mongo db", e);
        }

        if (mongoClient != null) {
            this.datastore = new Morphia().mapPackage(PACKAGE).createDatastore(mongoClient, DB);
            LOG.info("Created DataStore for MongoDB: " + DB);
        } else {
            LOG.error("Failed to created morphia instance");
        }
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.datastore = new Morphia().mapPackage(PACKAGE).createDatastore(mongoClient, DB);
    }

    public void save(Object object) {
        this.datastore.save(object);
    }

    public void delete(Object object) {
        this.datastore.delete(object);
    }

    public void deleteCollection(final Class<?> clazz) {
        this.datastore.delete(this.datastore.createQuery(clazz));
    }

    public AbstractJob findAbstractJobByName(String jobName) {
        return this.datastore.find(AbstractJob.class).field("name").equal(jobName).get();
    }

    public List<Confirmation> findAllPendingActivatations() {
        //TODO Refactoring
        //Confirmation.find("SELECT c FROM Confirmation c WHERE confirmType = ? AND DATE(NOW()) > (DATE(created) + 2)", ConfirmationType.ACTIVATION).fetch();
        return null;
    }

    public List<User> findAllNotifiableUsers() {
        return this.datastore.find(User.class).field("active").equal(true).field("sendGameTips").equal(true).asList();
    }

    public List<Game> findAllNotifiableGames() {
        //TODO Refactoring
        //Game.find("SELECT g FROM Game g WHERE informed = ? AND ( TIMESTAMPDIFF(MINUTE,kickoff,now()) > 1 )", false).fetch();
        return null;
    }

    public Playday findPlaydaybByNumber(int number) {
        return this.datastore.find(Playday.class).field("number").equal(number).get();
    }

    public List<Game> findAllGamesWithNoResult() {
        //TODO Refactoring
        // Game.find("SELECT g FROM Game g WHERE ended != 1 AND ( TIMESTAMPDIFF(MINUTE,kickoff,now()) > 90 ) AND homeTeam_id != '' AND awayTeam_id != '' AND webserviceID != ''").fetch();
        return Collections.EMPTY_LIST;
    }

    public List<Extra> findAllExtrasEndingToday() {
        //TODO Refactoring
        // Extra.find("SELECT e FROM Extra e WHERE DATE(ending) = DATE(NOW())").fetch();
        return null;
    }

    public List<Game> findAllGamesEndingToday() {
        //TODO Refactoring
        // Game.find("SELECT g FROM Game g WHERE DATE(kickoff) = DATE(NOW())").fetch();
        return null;
    }

    public List<User> findAllRemindableUsers() {
        return this.datastore.find(User.class).field("reminder").equal(true).field("active").equal(true).asList();
    }

    public GameTip findGameTipByGameAndUser(User user, Game game) {
        return this.datastore.find(GameTip.class).field("game").equal(game).field("user").equal(user).get();
    }

    public ExtraTip findExtraTipByExtraAndUser(Extra extra, User user) {
        return this.datastore.find(ExtraTip.class).field("extra").equal(extra).field("user").equal(user).get();
    }

    public List<User> findAllAdmins() {
        return this.datastore.find(User.class).field("admin").equal(true).asList();
    }

    public List<User> findUsersByNotificationAndActive() {
        return this.datastore.find(User.class).field("active").equal(true).field("notification").equal(true).asList();
    }

    public Game findGameFirstGame() {
        return this.datastore.find(Game.class).field("number").equal(1).get();
    }

    public List<User> findTopThreeUsers() {
        return this.datastore.find(User.class).field("active").equal(true).order("place").limit(3).asList();
    }

    public List<User> findSendableUsers() {
        return this.datastore.find(User.class).field("sendStandings").equal(true).asList();
    }

    public List<GameTip> findGameTipsByUser(User user) {
        return this.datastore.find(GameTip.class).field("user").equal(user).asList();
    }

    public ResultStatistic findResultStatisticByUserAndResult(User user, String score) {
        return this.datastore.find(ResultStatistic.class).field("user").equal(user).field("score").equal(score).get();
    }

    public GameStatistic findGameStatisticByPlaydayAndResult(Playday playday, Object key) {
        return this.datastore.find(GameStatistic.class).field("pladay").equal(playday).field("gameResult").equal((String) key).get();
    }

    public GameTipStatistic findGameTipStatisticByPlayday(Playday playday) {
        return this.datastore.find(GameTipStatistic.class).field("playday").equal(playday).get();
    }

    public UserStatistic findUserStatisticByPlaydayAndUser(Playday playday, User user) {
        return this.datastore.find(UserStatistic.class).field("playday").equal(playday).field("user").equal(user).get();
    }

    public List<UserStatistic> findUserStatisticByPlaydayOrderByPlaydayPoints(Playday playday) {
        return this.datastore.find(UserStatistic.class).field("playday").equal(playday).order("playdayPoints").asList();
    }

    public List<UserStatistic> findUserStatisticByPlaydayOrderByPoints(Playday playday) {
        return this.datastore.find(UserStatistic.class).field("playday").equal(playday).order("points").asList();
    }

    public List<GameTip> findGameTipByGame(Game game) {
        return this.datastore.find(GameTip.class).field("game").equal(game).asList();
    }

    public PlaydayStatistic findPlaydayStatisticByPlaydayAndResult(Playday playday, Object key) {
        return this.datastore.find(PlaydayStatistic.class).field("pladay").equal(playday).field("gameResult").equal((String) key).get();
    }

    public User findUserByEmail(String email) {
        return this.datastore.find(User.class).field("email").equal(email).get();
    }

    public User findUserByUsername(String username) {
        return this.datastore.find(User.class).field("username").equal(username).get();
    }

    public List<Confirmation> findAllConfirmation() {
        return this.datastore.find(Confirmation.class).asList();
    }

    public Settings findSettings() {
        Settings settings = (Settings) ninjaCache.get("settings");
        if (settings == null) {
            settings = this.datastore.find(Settings.class).field("appName").equal(Constants.APPNAME.value()).get();
            ninjaCache.add("settings", settings);
        }

        return settings;
    }

    public User findUserByPlace(int place) {
        return this.datastore.find(User.class).field("place").equal(place).get();
    }
    
    public Team findTeamByReference(final String reference) {
        Team team = null;
        if (StringUtils.isNotBlank(reference)) {
            final String[] references = reference.split("-");
            if ((references != null) && (references.length == 3)) {
                if ("B".equals(references[0])) {
                    final Bracket bracket = findBracketByNumber(references[1]);
                    if (bracket != null) {
                        team = commonService.getTeamByPlace(Integer.parseInt(references[2]), bracket);
                    }
                } else if ("G".equals(references[0])) {
                    final Game aGame = findGameByNumber(references[1]);
                    if ((aGame != null) && aGame.isEnded()) {
                        if ("W".equals(references[2])) {
                            team = commonService.getWinner(aGame);
                        } else if ("L".equals(references[2])) {
                            team = commonService.getLoser(aGame);
                        }
                    }
                }
            }
        }

        return team;
    }

    public Game findGameByNumber(String number) {
        return this.datastore.find(Game.class).field("number").equal(number).get();
    }

    public Bracket findBracketByNumber(String number) {
        return this.datastore.find(Bracket.class).field("number").equal(number).get();
    }

    public void saveScore(final Game game, final String homeScore, final String awayScore, final String extratime, String homeScoreExtratime, String awayScoreExtratime) {
        final int[] points = resultService.getPoints(Integer.parseInt(homeScore), Integer.parseInt(awayScore));
        game.setHomePoints(points[0]);
        game.setAwayPoints(points[1]);
        game.setHomeScore(homeScore);
        game.setAwayScore(awayScore);
        if (validationService.isValidScore(homeScoreExtratime, awayScoreExtratime )) {
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
            notificationService.sendNotfications(game);
            game.setEnded(true);
        }
        save(game);
    }

    public void saveGameTip(final Game game, final int homeScore, final int awayScore, User user) {
        GameTip gameTip = findGameTipByGameAndUser(user, game);
        if (commonService.gameIsTippable(game) && validationService.isValidScore(String.valueOf(homeScore), String.valueOf(awayScore))) {
            if (gameTip == null) {
                gameTip = new GameTip();
                gameTip.setGame(game);
                gameTip.setUser(user);
            }
            gameTip.setPlaced(new Date());
            gameTip.setHomeScore(homeScore);
            gameTip.setAwayScore(awayScore);
            save(gameTip);
            LOG.info("Tipp placed - " + user.getEmail() + " - " + gameTip);
        }
    }

    public List<Map<User, List<GameTip>>> findPlaydayTips(final Playday playday, final List<User> users) {
        final List<Map<User, List<GameTip>>> tips = new ArrayList<Map<User, List<GameTip>>>();

        for (final User user : users) {
            final Map<User, List<GameTip>> userTips = new HashMap<User, List<GameTip>>();
            final List<GameTip> gameTips = new ArrayList<GameTip>();
            for (final Game game : playday.getGames()) {
                GameTip gameTip = findGameTipByGameAndUser(user, game);
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
    public List<Map<User, List<ExtraTip>>> findExtraTips(final List<User> users, final List<Extra> extras) {
        final List<Map<User, List<ExtraTip>>> tips = new ArrayList<Map<User, List<ExtraTip>>>();
    
        for (final User user : users) {
            final Map<User, List<ExtraTip>> userTips = new HashMap<User, List<ExtraTip>>();
            final List<ExtraTip> extraTips = new ArrayList<ExtraTip>();
            for (final Extra extra : extras) {
                ExtraTip extraTip = findExtraTipByExtraAndUser(extra, user);
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

    public Playday findCurrentPlayday () {
        Playday playday = (Playday) ninjaCache.get("currentPlayday");
        if (playday == null) {
            playday = this.datastore.find(Playday.class).field("current").equal(true).get();
            if (playday == null) {
                playday = this.datastore.find(Playday.class).field("number").equal(1).get();
            }
            ninjaCache.add("currentPlayday", playday);
        }

        return playday;
    }

    public void saveExtraTip(final Extra extra, final Team team, User user) {
        if (team != null) {
            ExtraTip extraTip = findExtraTipByExtraAndUser(extra, user);
            if (extraTip == null) {
                extraTip = new ExtraTip();
            }

            extraTip.setUser(user);
            extraTip.setExtra(extra);
            extraTip.setAnswer(team);
            save(extraTip);
            LOG.info("Stored extratip - " + user.getEmail() + " - " + extraTip);
        }
    }

    public boolean appIsInizialized() {
        return findSettings() != null ? true : false;
    }

    public List<User> findAllActiveUsers() {
        return this.datastore.find(User.class).field("active").equal(true).asList();
    }

    public List<Playday> findAllPlaydaysOrderByNumber() {
        return this.datastore.find(Playday.class).order("number").asList();
    }

    public List<Extra> findAllExtras() {
        return this.datastore.find(Extra.class).asList();
    }

    public List<Team> findAllTeams() {
        return this.datastore.find(Team.class).asList();
    }

    public List<Game> findGamesByHomeTeam(Team team) {
        return this.datastore.find(Game.class).field("homeTeam").equal(team).asList();
    }

    public List<Game> findGamesByAwayTeam(Team team) {
        return this.datastore.find(Game.class).field("awayTeam").equal(team).asList();
    }

    public List<User> findAllActiveUsersOrdered() {
        return this.datastore.find(User.class).field("active").equal(true).order("points, correctResults, correctDifferences, correctTrends, correctExtraTips").asList();
    }

    public List<Bracket> findAllBrackets() {
        return this.datastore.find(Bracket.class).asList();
    }

    public Game findGameById(String gameId) {
        return this.datastore.get(Game.class, gameId);
    }

    public List<Bracket> findAllUpdatableBrackets() {
        return this.datastore.find(Bracket.class).field("updatable").equal(true).asList();
    }

    public List<Team> findTeamsByBracketOrdered(Bracket bracket) {
        return this.datastore.find(Team.class).field("bracket").equal(bracket).order("points, goalsDiff, goalsFor").asList();
    }
    
    public List<Team> findTeamsByBracket(Bracket bracket) {
        return this.datastore.find(Team.class).field("bracket").equal(bracket).asList();
    }

    public List<Game> findGamesByPlayoffAndEndedAndBracket() {
        return this.datastore.find(Game.class).field("playoff").equal(true).field("ended").equal(false).field("bracket").equal(null).asList();
    }

    public List<Game> findReferencedGames(String bracketString) {
        Pattern pattern = Pattern.compile(bracketString);
        List<Game> games = this.datastore.find(Game.class).filter("homeReference", pattern).asList();
        games.addAll(this.datastore.find(Game.class).filter("awayReference", pattern).asList());
        
        return games;
    }

    public List<Game> findAllNonPlayoffGames() {
        return this.datastore.find(Game.class).field("playoff").equal(false).asList();
    }

    public List<Game> findAllPlayoffGames() {
        return this.datastore.find(Game.class).field("playoff").equal(true).asList();
    }

    public long countAllUsers() {
        return this.datastore.find(User.class).countAll();
    }

    public List<User> findActiveUsers(int limit) {
        return this.datastore.find(User.class).field("active").equal(true).order("place").limit(limit).asList();
    }

    public List<User> findAllActiveUsersOrderedByPlace() {
        return this.datastore.find(User.class).field("active").equal(true).order("place").asList();
    }

    public Team findTeamById(String teamId) {
        return this.datastore.get(Team.class, teamId);
    }

    public Bracket findBracketById(String bracketid) {
        return this.datastore.get(Bracket.class, bracketid);
    }

    public Extra findExtaById(String bonusTippId) {
        return this.datastore.get(Extra.class, bonusTippId);
    }

    public List<User> findUsersOrderByUsername() {
        return this.datastore.find(User.class).field("active").equal(true).order("username").asList();
    }

    public User findUserById(String userId) {
        return this.datastore.get(User.class, userId);
    }

    public Confirmation findConfirmationByTypeAndUser(ConfirmationType confirmationType, User user) {
        return this.datastore.find(Confirmation.class).field("confirmationType").equal(confirmationType).field("user").equal(user).get();
    }

    public List<Game> findAllGames() {
        return this.datastore.find(Game.class).asList();
    }

    public Confirmation findConfirmationByToken(String token) {
        return this.datastore.find(Confirmation.class).field("token").equal(token).get();
    }

    public User findUserByEmailAndActive(String email) {
        return this.datastore.find(User.class).field("active").equal(true).field("email").equal(email).get();
    }

    public User findUserByUsernameOrEmail(String username) {
        Query<User> query = this.datastore.find(User.class);
        query.or(query.criteria("username").equal(username), query.criteria("email").equal(username));
        query.and(query.criteria("active").equal(true));

        return query.get();
    }

    public long countAllGames() {
        return this.datastore.find(Game.class).countAll();
    }

    public long countAllExtras() {
        return this.datastore.find(Extra.class).countAll();
    }

    public void dropDatabase() {
        this.datastore.getDB().dropDatabase();
    }

    public List<Game> findGamesByBracket(Bracket bracket) {
        return this.datastore.find(Game.class).field("bracket").equal(bracket).asList();
    }

    public List<Game> findGamesByPlayday(Playday playday) {
        return this.datastore.find(Game.class).field("playday").equal(playday).asList();
    }

    public List<ExtraTip> findExtraTipsByUser(User user) {
        return this.datastore.find(ExtraTip.class).field("user").equal(user).field("points").greaterThan(0).asList();
    }

    public List<UserStatistic> findUserStatisticByUser(User user) {
        return this.datastore.find(UserStatistic.class).field("user").equal(user).order("playday").asList();
    }

    public List<AbstractJob> findAllAbstractJobs() {
        return this.datastore.find(AbstractJob.class).asList();
    }

    public void deleteResultsStatisticByUser(User user) {
        this.datastore.delete(this.datastore.find(ResultStatistic.class).field("user").equal(user).get());
    }

    public List<GameStatistic> findAllGameStatistics() {
        return this.datastore.find(GameStatistic.class).asList();
    }

    public List<GameTipStatistic> findGameTipStatisticsOrderByPlayday() {
        return this.datastore.find(GameTipStatistic.class).order("playday").asList();
    }
}