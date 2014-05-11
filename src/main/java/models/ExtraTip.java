package models;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * 
 * @author svenkubiak
 *
 */
@Entity(value = "extratips", noClassnameStored = true)
public class ExtraTip implements Serializable {
    private static final long serialVersionUID = -2648500340981679571L;

    @Id
    private ObjectId id;

    @Reference
    private User user;

    @Reference
    private Extra extra;

    @Reference
    public Team answer;

    private int points;

    public ObjectId getId() {
        return id;
    }

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