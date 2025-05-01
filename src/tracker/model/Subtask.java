package tracker.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, int id) {
        super(name, description, id);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, String status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, int id, String status) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, String startTime, int minutesToComplete) {
        super(name, description, startTime, minutesToComplete);
        this.epicId = epicId;

    }

    public Subtask(String name, String description, int epicId, int id, String status, String startTime,
                   int minutesToComplete) {
        super(name, description, id, status, startTime, minutesToComplete);
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "epicId='" + epicId + "', "
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

    public int getEpicId() {
        return epicId;
    }
}