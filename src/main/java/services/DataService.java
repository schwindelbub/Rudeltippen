package services;

import java.net.UnknownHostException;
import java.util.List;

import models.AbstractJob;
import models.Confirmation;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
import models.Playday;
import models.User;
import models.statistic.GameStatistic;
import models.statistic.GameTipStatistic;
import models.statistic.PlaydayStatistic;
import models.statistic.ResultStatistic;
import models.statistic.UserStatistic;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.mongodb.MongoClient;

@Singleton
public class DataService {
    private static final Logger LOG = LoggerFactory.getLogger(DataService.class);
    private static final String PACKAGE = "models";
    private static final String DB = "rudeltippen";
    private Datastore datastore;

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

    public void deleteAll(final Class<?> clazz) {
        this.datastore.delete(this.datastore.createQuery(clazz));
    }

    public AbstractJob findAbstractJobByName(String jobName) {
        return this.datastore.find(AbstractJob.class).field("name").equal(jobName).get();
    }

    public List<Confirmation>  findAllPendingActivatations() {
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
        return null;
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
        //TODO Refactoring
        // GameTip.find("byGameAndUser", game, user).first();
        return null;
    }

    public ExtraTip findExtraTipByExtraAndUser(Extra extra, User user) {
        //TODO Refactoring
        // ExtraTip.find("byExtraAndUser", extra, user).first();
        return null;
    }

    public List<User> findAllAdmins() {
        return this.datastore.find(User.class).field("admin").equal(true).asList();
    }

    public Object [] getPlaydayStatistics(Playday playday) {
        //TODO Refactoring
        //        Object result = null;
        //        Object [] values = null;
        //
        //        result = JPA.em()
        //                .createQuery("SELECT " +
        //                        "SUM(playdayPoints) AS points, " +
        //                        "SUM(playdayCorrectTips) AS tips, " +
        //                        "SUM(playdayCorrectDiffs) AS diffs," +
        //                        "SUM(playdayCorrectTrends) AS trends, " +
        //                        "ROUND(AVG(playdayPoints)) AS avgPoints " +
        //                        "FROM UserStatistic u WHERE u.playday.id = :playdayID")
        //                        .setParameter("playdayID", playday.getId())
        //                        .getSingleResult();
        //
        //        if (result != null) {
        //            values = (Object[]) result;
        //        }
        //
        //        return values;
        return null;
    }

    public List<Object[]> getGameStatistics() {
        //TODO Refactoring
        //        List<Object []> results = JPA.em()
        //                .createQuery(
        //                        "SELECT " +
        //                                "SUM(resultCount) AS counts, " +
        //                                "gameResult AS result " +
        //                                "FROM GameStatistic g " +
        //                                "GROUP BY gameResult " +
        //                        "ORDER BY counts DESC").getResultList();
        //
        //        return results;
        return null;
    }

    public Object []  getAscendingStatistics(final Playday playday, final User user) {
        //TODO Refactoring
        //        Object result = null;
        //        Object [] values = null;
        //
        //        result = JPA.em()
        //                .createQuery(
        //                        "SELECT " +
        //                                "SUM(playdayPoints) AS points, " +
        //                                "SUM(playdayCorrectTips) AS correctTips, " +
        //                                "SUM(playdayCorrectDiffs) AS correctDiffs, " +
        //                                "SUM(playdayCorrectTrends) AS correctTrends " +
        //                                "FROM UserStatistic u " +
        //                        "WHERE u.playday.id <= :playdayID AND u.user.id = :userID")
        //                        .setParameter("playdayID", playday.getId())
        //                        .setParameter("userID", user.getId())
        //                        .getSingleResult();
        //
        //        if (result != null) {
        //            values = (Object[]) result;
        //        }
        //
        //        return values;
        return null;
    }

    public List<Object[]> getResultsStatistic() {
        //TODO Refactoring
        //        List<Object []> results = JPA.em()
        //                .createQuery("SELECT " +
        //                        "SUM(resultCount) AS counts, " +
        //                        "gameResult AS result " +
        //                        "FROM PlaydayStatistic p " +
        //                        "GROUP BY gameResult " +
        //                        "ORDER BY counts DESC").getResultList();
        //
        //        return results;
        return null;
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

    public List<GameTip> findGameTipByUser(User user) {
        //TODO Refactoring
        //GameTip.find("byUser", user).fetch();
        return null;
    }

    public ResultStatistic findResultStatisticByUserAndResult(User user, String score) {
        //TODO Refactoring
        //ResultStatistic.find("byUserAndResult", user, score).first();
        return null;
    }

    public GameStatistic findGameStatisticByPlaydayAndResult(Playday playday, Object key) {
        //TODO Refactoring
        //GameStatistic.find("byPlaydayAndGameResult", playday, entry.getKey()).first();
        return null;
    }

    public GameTipStatistic findGameTipStatisticByPlayday() {
        //TODO Refactoring
        // GameTipStatistic.find("byPlayday", playday).first();
        return null;
    }

    public UserStatistic findUserStatisticByPlaydayAndUser(Playday playday, User user) {
        //TODO Refactoring
        // UserStatistic.find("byPlaydayAndUser", playday, user).first();
        return null;
    }

    public List<UserStatistic> findUserStatisticByPlaydayOrderByPlaydayPoints(Playday playday) {
        //TODO Refactoring
        // UserStatistic.find("SELECT u FROM UserStatistic u WHERE playday = ? ORDER BY playdayPoints DESC", playday).fetch();
        return null;
    }

    public List<UserStatistic> findUserStatisticByPlaydayOrderByPoints(Playday playday) {
        //TODO Refactoring
        //  UserStatistic.find("SELECT u FROM UserStatistic u WHERE playday = ? ORDER BY points DESC", playday).fetch();
        return null;
    }

    public List<GameTip> findGameTipByGame(Game game) {
        //TODO Refactoring
        // GameTip.find("byGame", game).fetch();
        return null;
    }

    public PlaydayStatistic findPlaydayStatisticByPlaydayAndResult(Playday playday, Object key) {
        //TODO Refactoring
        // PlaydayStatistic.find("byPlaydayAndGameResult", playday, entry.getKey()).first();
        return null;
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
}