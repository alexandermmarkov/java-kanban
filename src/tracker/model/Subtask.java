package tracker.model;

public class Subtask extends Task {
    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Subtask(String name, String description, Epic epic, int id) {
        super(name, description, id);
        this.epic = epic;
    }

    public Subtask(String name, String description, Epic epic, String status) {
        super(name, description, status);
        this.epic = epic;
    }

    public Subtask(String name, String description, Epic epic, int id, String status) {
        super(name, description, id, status);
        this.epic = epic;
    }

    public Subtask(String name, String description, Epic epic, String startTime, int minutesToComplete) {
        super(name, description, startTime, minutesToComplete);
        this.epic = epic;

    }

    public Subtask(String name, String description, Epic epic, int id, String status, String startTime, int minutesToComplete) {
        super(name, description, id, status, startTime, minutesToComplete);
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "Subtask{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "epic=" + epic + "', "
                + "status='" + status + "'"
                + (getDuration().isPresent() ? ", duration='" + duration.toMinutes() + "'" : "")
                + (getStartTime().isPresent() ? ", startTime='" + startTime.format(DATE_FORMATTER) + "'" : "")
                + (getEndTime().isPresent() ? ", endTime='" + getEndTime().get().format(DATE_FORMATTER) + "'" : "")
                + '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public Epic getEpic() {
        return epic;
    }
}
