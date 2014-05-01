package models;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "gametips", noClassnameStored = true)
public class GameTip {
    @Id
    private ObjectId id;

    @Reference
    private User user;

    @Reference
    private Game game;

    @Size(min = 0, max = 99)
    private int homeScore;

    @Size(min = 0, max = 99)
    private int awayScore;

    @NotNull
    private Date placed;

    @Min(value = 0)
    private int points;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public Date getPlaced() {
        return placed;
    }

    public void setPlaced(Date placed) {
        this.placed = placed;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}