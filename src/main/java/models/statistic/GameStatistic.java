package models.statistic;

import javax.validation.constraints.NotNull;

import models.Playday;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "gamestatistics", noClassnameStored = true)
public class GameStatistic {
    @Id
    private ObjectId id;

    @Reference
    private Playday playday;

    @NotNull
    private String gameResult;

    private int resultCount;

    public Playday getPlayday() {
        return playday;
    }

    public void setPlayday(Playday playday) {
        this.playday = playday;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}