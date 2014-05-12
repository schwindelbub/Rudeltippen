package models;

import java.io.Serializable;
import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * 
 * @author svenkubiak
 *
 */
@Entity(value = "jobs", noClassnameStored = true)
public class AbstractJob implements Serializable {
    private static final long serialVersionUID = -181239072023766955L;

    @Id
    private ObjectId id;

    private String name;
    private String scheduled;
    private String description;
    private Date executed;

    private boolean active;
    
    public AbstractJob() {
    }
    
    public AbstractJob(String name, String scheduled, String description) {
        this.name = name;
        this.scheduled = scheduled;
        this.description = description;
        this.executed = null;
        this.active = true;
    }

    public ObjectId getId() {
        return id;
    }
    
    public String getScheduled() {
        return scheduled;
    }

    public void setScheduled(String scheduled) {
        this.scheduled = scheduled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public Date getExecuted() {
        return executed;
    }

    public void setExecuted(Date executed) {
        this.executed = executed;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}