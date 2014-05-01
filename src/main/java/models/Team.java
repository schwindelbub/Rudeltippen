package models;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "teams", noClassnameStored = true)
public class Team {
    @Id
    private ObjectId id;

    @NotNull
    private String name;

    @Reference
    private Bracket bracket;

    @Reference(value = "team_homegames", lazy = true)
    private List<Game> homeGames;

    @Reference(value = "team_awaygames", lazy = true)
    private List<Game> awayGames;

    @NotNull
    private String flag;

    private int points;
    private int goalsFor;
    private int goalsAgainst;
    private int goalsDiff;
    private int gamesPlayed;
    private int gamesWon;
    private int gamesDraw;
    private int gamesLost;
    private int place;
    private int previousPlace;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Bracket getBracket() {
        return this.bracket;
    }

    public void setBracket(final Bracket bracket) {
        this.bracket = bracket;
    }

    public List<Game> getHomeGames() {
        return this.homeGames;
    }

    public void setHomeGames(final List<Game> homeGames) {
        this.homeGames = homeGames;
    }

    public List<Game> getAwayGames() {
        return this.awayGames;
    }

    public void setAwayGames(final List<Game> awayGames) {
        this.awayGames = awayGames;
    }

    public String getFlag() {
        return this.flag;
    }

    public void setFlag(final String flag) {
        this.flag = flag;
    }

    public int getPoints() {
        return this.points;
    }

    public void setPoints(final int points) {
        this.points = points;
    }

    public int getGoalsFor() {
        return this.goalsFor;
    }

    public void setGoalsFor(final int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public int getGoalsAgainst() {
        return this.goalsAgainst;
    }

    public void setGoalsAgainst(final int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

    public int getGoalsDiff() {
        return this.goalsDiff;
    }

    public void setGoalsDiff(final int goalsDiff) {
        this.goalsDiff = goalsDiff;
    }

    public int getGamesPlayed() {
        return this.gamesPlayed;
    }

    public void setGamesPlayed(final int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return this.gamesWon;
    }

    public void setGamesWon(final int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesDraw() {
        return this.gamesDraw;
    }

    public void setGamesDraw(final int gamesDraw) {
        this.gamesDraw = gamesDraw;
    }

    public int getGamesLost() {
        return this.gamesLost;
    }

    public void setGamesLost(final int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public int getPlace() {
        return this.place;
    }

    public void setPlace(final int place) {
        this.place = place;
    }

    public int getPreviousPlace() {
        return this.previousPlace;
    }

    public void setPreviousPlace(final int previousPlace) {
        this.previousPlace = previousPlace;
    }
    //TODO Refactoring
    //    public String nameUnescaped() {
    //        if (StringUtils.isNotBlank(this.name)) {
    //            final String name = Messages.get(this.name);
    //            return StringEscapeUtils.unescapeHtml(name);
    //        }
    //
    //        return "";
    //    }
}