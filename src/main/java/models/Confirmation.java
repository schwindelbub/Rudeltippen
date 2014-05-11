package models;

import java.io.Serializable;
import java.util.Date;

import models.enums.ConfirmationType;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * 
 * @author svenkubiak
 *
 */
@Entity(value = "confirmations", noClassnameStored = true)
public class Confirmation implements Serializable {
    private static final long serialVersionUID = -5965149177345129285L;

    @Id
    private ObjectId id;

    @Reference
    private User user;

    private String token;
    private String confirmValue;

    private ConfirmationType confirmType;

    private Date created;

    public ObjectId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ConfirmationType getConfirmType() {
        return confirmType;
    }

    public void setConfirmType(ConfirmationType confirmType) {
        this.confirmType = confirmType;
    }

    public String getConfirmValue() {
        return confirmValue;
    }

    public void setConfirmValue(String confirmValue) {
        this.confirmValue = confirmValue;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}