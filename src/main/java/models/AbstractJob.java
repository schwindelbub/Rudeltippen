package models;

import javax.validation.constraints.NotNull;

import org.mongodb.morphia.annotations.Entity;

@Entity(value = "jobs", noClassnameStored = true)
public class AbstractJob {
    @NotNull
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