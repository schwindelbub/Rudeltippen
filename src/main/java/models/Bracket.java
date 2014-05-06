package models;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * 
 * @author svenkubiak
 *
 */
@Entity(value = "brackets", noClassnameStored = true)
public class Bracket {
    @Id
    private ObjectId id;

    private String name;

    @Reference("bracket_teams")
    private List<Team> teams;

    @Reference("bracket_games")
    private List<Game> games;

    private int number;
    
    private boolean updateble;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(final List<Team> teams) {
        this.teams = teams;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(final List<Game> games) {
        this.games = games;
    }

    public boolean isUpdateble() {
        return updateble;
    }

    public void setUpdateble(boolean updateble) {
        this.updateble = updateble;
    }

    //TODO Refactoring
    public boolean allGamesEnded() {
        for (final Game game : games) {
            if (!game.isEnded()) {
                return false;
            }
        }

        return true;
    }

    //TODO Refactoring
    public Team getTeamByPlace(final int place) {
        int i = 1;
        for (final Team team : teams) {
            if  (i == place) {
                return team;
            }
            i++;
        }

        return null;
    }
}