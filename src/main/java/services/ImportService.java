package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Bracket;
import models.Extra;
import models.Game;
import models.Playday;
import models.Team;
import models.enums.Constants;

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
    private static final String BRACKET = "bracket";
    private static final String PLAYOFF = "playoff";
    private static final String UPDATABLE = "updatable";
    private static final String NUMBER = "number";

    @Inject
    private DataService dataService;

    public void loadInitialData() {
        Map<String, Bracket> brackets = loadBrackets();
        Map<String, Team> teams = loadTeams(brackets);
        Map<String, Playday> playdays = loadPlaydays();
        loadGames(playdays, teams, brackets);
        loadExtras(teams);

        setReferences();
    }

    private void setReferences() {
        List<Bracket> brackets = dataService.findAllBrackets();
        for (Bracket bracket : brackets) {
            List<Game> games = dataService.findGamesByBracket(bracket);
            List<Team> teams = dataService.findTeamsByBracket(bracket);

            bracket.setGames(games);
            bracket.setTeams(teams);
            dataService.save(bracket);
        }

        List<Playday> playdays = dataService.findAllPlaydaysOrderByNumber();
        for (Playday playday : playdays) {
            List<Game> games = dataService.findGamesByPlayday(playday);

            playday.setGames(games);
            dataService.save(playday);
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
            dataService.save(bracket);

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
            dataService.save(extra);
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
            dataService.save(game);
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
            dataService.save(playday);

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
            dataService.save(team);

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