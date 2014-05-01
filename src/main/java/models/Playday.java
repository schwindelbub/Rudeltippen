package models;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import models.statistic.GameStatistic;
import models.statistic.GameTipStatistic;
import models.statistic.UserStatistic;

@Entity(value = "playdays", noClassnameStored = true)
public class Playday {
    @Id
    private ObjectId id;

    @Reference(value = "playday_games")
    private List<Game> games;

    @Reference(value = "playday_userstatistics", lazy = true)
    private List<UserStatistic> userStatistics;

    @Reference(value = "playday_gametipstatistics", lazy = true)
    private List<GameTipStatistic> gameTipStatistics;

    @Reference(value = "playday_gamestatistic", lazy = true)
    private List<GameStatistic> gameStatistic;

    @NotNull
    private String name;

    @Min(value = 1)
    private int number;

    private boolean playoff;
    private boolean current;

    public boolean isCurrent() {
        return this.current;
    }

    public void setCurrent(final boolean current) {
        this.current = current;
    }

    public List<Game> getGames() {
        return this.games;
    }

    public void setGames(final List<Game> games) {
        this.games = games;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public boolean isPlayoff() {
        return this.playoff;
    }

    public void setPlayoff(final boolean playoff) {
        this.playoff = playoff;
    }

    public boolean isTippable() {
        for (final Game game : this.games){
            if (game.isTippable()) {
                return true;
            }
        }
        return false;
    }

    public boolean allGamesEnded() {
        for (final Game game : this.games) {
            if (!game.isEnded()) {
                return false;
            }
        }
        return true;
    }

    public List<UserStatistic> getUserStatistics() {
        return userStatistics;
    }

    public void setUserStatistics(List<UserStatistic> userStatistics) {
        this.userStatistics = userStatistics;
    }

    public List<GameTipStatistic> getGameTipStatistics() {
        return gameTipStatistics;
    }

    public void setGameTipStatistics(List<GameTipStatistic> gameTipStatistics) {
        this.gameTipStatistics = gameTipStatistics;
    }

    public List<GameStatistic> getGameStatistic() {
        return gameStatistic;
    }

    public void setGameStatistic(List<GameStatistic> gameStatistic) {
        this.gameStatistic = gameStatistic;
    }
}