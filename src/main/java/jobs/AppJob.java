package jobs;

public class AppJob {
    private String description;
    private String executed;

    public String getExecuted() {
        return executed;
    }

    public void setExecuted(final String executed) {
        this.executed = executed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}