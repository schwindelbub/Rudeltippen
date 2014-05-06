package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * 
 * @author svenkubiak
 *
 */
@Entity(value = "jobs", noClassnameStored = true)
public class AbstractJob {
    @Id
    private ObjectId id;

    private String name;
    
    private boolean active;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}