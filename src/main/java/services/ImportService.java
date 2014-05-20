package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.AbstractJob;
import models.Bracket;
import models.Extra;
import models.Game;
import models.Playday;
import models.Settings;
import models.Team;
import models.User;
import models.enums.Avatar;
import models.enums.Constants;
import ninja.morphia.NinjaMorphia;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * 
 * @author skubiak
 *
 */
@Singleton
public class ImportService {
    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);
    private static final String ADMIN = "admin";
    private static final String BRACKET = "bracket";
    private static final String PLAYOFF = "playoff";
    private static final String UPDATABLE = "updatable";
    private static final String NUMBER = "number";

    @Inject
    private DataService dataService;
    
    @Inject
    private NinjaMorphia ninjaMorphia;
    
    @Inject
    private AuthService authService;
    
    @Inject
    private CommonService commonService;
    
    @Inject
    private I18nService i18nService;

    public void loadInitialData() {
        Map<String, Bracket> brackets = loadBrackets();
        Map<String, Team> teams = loadTeams(brackets);
        Map<String, Playday> playdays = loadPlaydays();
        loadGames(playdays, teams, brackets);
        loadExtras(teams);
        loadSettingsAndAdmin();
        initJobs();

        setReferences();
    }

    public void loadSettingsAndAdmin() {
        final List<Game> prePlayoffGames = dataService.findAllNonPlayoffGames();
        final List<Game> playoffGames = dataService.findAllPlayoffGames();
        boolean hasPlayoffs = false;
        if (playoffGames != null && !playoffGames.isEmpty()) {
            hasPlayoffs = true;
        }
        
        Settings settings = new Settings();
        settings.setAppName(Constants.APPNAME.get());
        settings.setPointsGameWin(3);
        settings.setPointsGameDraw(1);
        settings.setAppSalt(DigestUtils.sha512Hex(UUID.randomUUID().toString()));
        settings.setGameName("Rudeltippen");
        settings.setPointsTip(4);
        settings.setPointsTipDiff(2);
        settings.setPointsTipTrend(1);
        settings.setMinutesBeforeTip(5);
        settings.setPlayoffs(hasPlayoffs);
        settings.setNumPrePlayoffGames(prePlayoffGames.size());
        settings.setInformOnNewTipper(true);
        settings.setEnableRegistration(true);
        ninjaMorphia.save(settings);

        User user = new User();
        final String salt = DigestUtils.sha512Hex(UUID.randomUUID().toString());
        user.setSalt(salt);
        user.setEmail("admin@foo.bar");
        user.setUsername(ADMIN);
        user.setUserpass(authService.hashPassword(ADMIN, salt));
        user.setRegistered(new Date());
        user.setExtraPoints(0);
        user.setTipPoints(0);
        user.setPoints(0);
        user.setActive(true);
        user.setAdmin(true);
        user.setReminder(true);
        user.setNotification(true);
        user.setSendGameTips(true);
        user.setSendStandings(true);
        user.setCorrectResults(0);
        user.setCorrectDifferences(0);
        user.setCorrectTrends(0);
        user.setCorrectExtraTips(0);
        user.setPicture(commonService.getUserPictureUrl(Avatar.GRAVATAR, user));
        user.setAvatar(Avatar.GRAVATAR);
        ninjaMorphia.save(user);        
    }

    private void initJobs() {
        List<AbstractJob> abstractJobs = new ArrayList<AbstractJob>();
        abstractJobs.add(new AbstractJob(Constants.GAMETIPJOB.get(), i18nService.get("job.gametipjob.executed"), i18nService.get("job.gametipjob.description")));
        abstractJobs.add(new AbstractJob(Constants.KICKOFFJOB.get(), i18nService.get("job.playdayjob.executed"), i18nService.get("job.playdayjob.description")));
        abstractJobs.add(new AbstractJob(Constants.REMINDERJOB.get(), i18nService.get("job.reminderjob.executed"), i18nService.get("job.reminderjob.description")));
        abstractJobs.add(new AbstractJob(Constants.RESULTJOB.get(), i18nService.get("job.resultsjob.executed"), i18nService.get("job.resultsjob.descrption")));

        for (AbstractJob abstractJob : abstractJobs) {
            AbstractJob job = dataService.findAbstractJobByName(abstractJob.getName());
            if (job == null) {
                ninjaMorphia.save(abstractJob);
            }
        }
    }

    private void setReferences() {
        List<Bracket> brackets = ninjaMorphia.findAll(Bracket.class);
        for (Bracket bracket : brackets) {
            List<Game> games = dataService.findGamesByBracket(bracket);
            List<Team> teams = dataService.findTeamsByBracket(bracket);

            bracket.setGames(games);
            bracket.setTeams(teams);
            ninjaMorphia.save(bracket);
        }

        List<Playday> playdays = dataService.findAllPlaydaysOrderByNumber();
        for (Playday playday : playdays) {
            List<Game> games = dataService.findGamesByPlayday(playday);

            playday.setGames(games);
            ninjaMorphia.save(playday);
        }
    }

    private Map<String, Bracket> loadBrackets() {
        Map<String, Bracket> brackets = new HashMap<String, Bracket>();
        List<String> lines = readLines("brackets.json");

        for (String line : lines) {
            BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(line);
            Bracket bracket = new Bracket();
            bracket.setName(basicDBObject.getString("name"));
            bracket.setNumber(basicDBObject.getInt(NUMBER));
            bracket.setUpdatable(basicDBObject.getBoolean(UPDATABLE));
            ninjaMorphia.save(bracket);

            brackets.put(basicDBObject.getString("id"), bracket);
        }

        return brackets;
    }

    private void loadExtras(Map<String, Team> teams) {
        List<String> lines = readLines("extras.json");

        for (String line : lines) {
            BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(line);
            Extra extra = new Extra();
            extra.setPoints(basicDBObject.getInt("points"));
            extra.setQuestion(basicDBObject.getString("question"));
            extra.setExtraReference(basicDBObject.getString("extraReference"));
            extra.setQuestionShort(basicDBObject.getString("questionShort"));
            ninjaMorphia.save(extra);
        }
    }

    private void loadGames(Map<String, Playday> playdays, Map<String, Team> teams, Map<String, Bracket> brackets) {
        List<String> lines = readLines("games.json");

        for (String line : lines) {
            BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(line);
            Game game = new Game();
            game.setBracket(brackets.get(basicDBObject.getString(BRACKET)));
            game.setNumber(basicDBObject.getInt(NUMBER));
            game.setPlayoff(basicDBObject.getBoolean(PLAYOFF));
            game.setEnded(basicDBObject.getBoolean("ended"));
            game.setUpdatable(basicDBObject.getBoolean(UPDATABLE));
            game.setWebserviceID(basicDBObject.getString("webserviceID"));
            game.setHomeTeam(teams.get(basicDBObject.getString("homeTeam")));
            game.setHomeReference(basicDBObject.getString("homeReference"));
            game.setAwayReference(basicDBObject.getString("awayReference"));
            game.setAwayTeam(teams.get(basicDBObject.getString("awayTeam")));
            game.setPlayday(playdays.get(basicDBObject.getString("playday")));
            game.setKickoff(parseDate(basicDBObject.getString("kickoff"), "yyyy-MM-dd hh:mm:ss"));
            ninjaMorphia.save(game);
        }
    }

    private Date parseDate(String date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            LOG.error("Failed to pased date", e);
        }

        return null;
    }

    private Map<String, Playday> loadPlaydays() {
        Map<String, Playday> playdays = new HashMap<String, Playday>();
        List<String> lines = readLines("playdays.json");

        for (String line : lines) {
            BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(line);
            Playday playday = new Playday();
            playday.setName(basicDBObject.getString("name"));
            playday.setCurrent(basicDBObject.getBoolean("current"));
            playday.setCurrent(basicDBObject.getBoolean(PLAYOFF));
            playday.setNumber(basicDBObject.getInt(NUMBER));
            ninjaMorphia.save(playday);

            playdays.put(basicDBObject.getString("id"), playday);
        }

        return playdays;
    }

    private Map<String, Team> loadTeams(Map<String, Bracket> brackets) {
        Map<String, Team> teams = new HashMap<String, Team>();
        List<String> lines = readLines("teams.json");

        for (String line : lines) {
            BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(line);
            Team team = new Team();
            team.setName(basicDBObject.getString("name"));
            team.setFlag(basicDBObject.getString("flag"));
            team.setGamesPlayed(basicDBObject.getInt("gamesPlayed"));
            team.setGamesWon(basicDBObject.getInt("gamesWon"));
            team.setGamesDraw(basicDBObject.getInt("gamesDraw"));
            team.setGamesLost(basicDBObject.getInt("gamesLost"));
            team.setBracket(brackets.get(basicDBObject.getString(BRACKET)));
            ninjaMorphia.save(team);

            teams.put(basicDBObject.getString("id"), team);
        }

        return teams;
    }

    private List<String> readLines(String filename) {
        URL url = Resources.getResource(filename);
        List<String> lines = null;
        try {
            lines = IOUtils.readLines(new FileInputStream(new File(url.getPath())), Constants.ENCODING.get());
        } catch (IOException e) {
            LOG.error("Failed to read lines", e);
        }
        return lines;
    }
}