package models;

import java.io.Serializable;

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

    private boolean active;

    public ObjectId getId() {
        return id;
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

    public void setActive(final boolean active) {
        this.active = active;
    }
}