package models.statistic;

import models.Playday;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "gametipstatistics", noClassnameStored = true)
public class GameTipStatistic {
    @Id
    private ObjectId id;

    @Reference
    private Playday playday;

    private int points;
    private int correctTrends;
    private int correctTips;
    private int correctDiffs;
    private int avgPoints;

    public Playday getPlayday() {
        return playday;
    }

    public void setPlayday(Playday playday) {
        this.playday = playday;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getCorrectTrends() {
        return correctTrends;
    }

    public void setCorrectTrends(int correctTrends) {
        this.correctTrends = correctTrends;
    }

    public int getCorrectTips() {
        return correctTips;
    }

    public void setCorrectTips(int correctTips) {
        this.correctTips = correctTips;
    }

    public int getCorrectDiffs() {
        return correctDiffs;
    }

    public void setCorrectDiffs(int correctDiffs) {
        this.correctDiffs = correctDiffs;
    }

    public int getAvgPoints() {
        return avgPoints;
    }

    public void setAvgPoints(int avgPoints) {
        this.avgPoints = avgPoints;
    }
}