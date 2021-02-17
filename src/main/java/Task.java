public class Task {
    private boolean done;
    private String info;
    private boolean archived;

    /**
     * Constructor for Task
     * @param info task info
     */
    public Task(String info) {
        done = false;
        this.info = info;
    }
    /**
     * Accessor for task completion state
     * @return completion state
     */
    public boolean getCompletionState() {
        return done;
    }

    /**
     * Accessor for task info
     * @return task info
     */
    public String getTaskInfo() {
        return info;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", done ? "O" : "X", info);
    }

    /**
     * Sets the completion state for the task as done
     */
    public void setTaskAsDone() {
        done = true;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
