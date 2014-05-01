package models.statistic;

import javax.validation.constraints.NotNull;

import models.Playday;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "playdaystatistics", noClassnameStored = true)
public class PlaydayStatistic {
    @Id
    private ObjectId id;

    @Reference
    private Playday playday;

    @NotNull
    private String gameResult;

    private int resultCount;

    public Playday getPlayday() {
        return this.playday;
    }

    public void setPlayday(final Playday playday) {
        this.playday = playday;
    }

    public String getGameResult() {
        return this.gameResult;
    }

    public void setGameResult(final String gameResult) {
        this.gameResult = gameResult;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}