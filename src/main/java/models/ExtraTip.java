package models;

import javax.validation.constraints.Min;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "extratips", noClassnameStored = true)
public class ExtraTip {
    @Id
    private ObjectId id;

    @Reference
    private User user;

    @Reference
    private Extra extra;

    @Reference
    public Team answer;

    @Min(value = 0)
    private int points;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public Team getAnswer() {
        return answer;
    }

    public void setAnswer(Team answer) {
        this.answer = answer;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
