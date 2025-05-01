package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class Task {
    protected static int identificator;
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected int id;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public Task(String name, String description, int id) {
        this.id = id;
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public Task(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.valueOf(status);
    }

    public Task(String name, String description, int id, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.valueOf(status);
    }

    public Task(String name, String description, String startTime, int minutesToComplete) {
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
        this.startTime = LocalDateTime.parse(startTime, DATE_FORMATTER);
        duration = Duration.ofMinutes(minutesToComplete);
    }

    public Task(String name, String description, int id, String status, String startTime, int minutesToComplete) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.valueOf(status);
        this.startTime = LocalDateTime.parse(startTime, DATE_FORMATTER);
        duration = Duration.ofMinutes(minutesToComplete);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "status='" + status + "'"
                + (getDuration().isPresent() ? ", duration='" + duration.toMinutes() + "'" : "")
                + (getStartTime().isPresent() ? ", startTime='" + startTime.format(DATE_FORMATTER) + "'" : "")
                + (getEndTime().isPresent() ? ", endTime='" + getEndTime().get().format(DATE_FORMATTER) + "'" : "")
                + '}';
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public static int getNewIdentificator() {
        return ++identificator;
    }

    public static void resetIdentificator() {
        identificator = 0;
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

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = TaskStatus.valueOf(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Optional<Duration> getDuration() {
        if (duration != null) return Optional.of(duration);
        return Optional.empty();
    }

    public void setDuration(int durationInMinutes) {
        duration = Duration.ofMinutes(durationInMinutes);
    }

    public Optional<LocalDateTime> getStartTime() {
        if (startTime != null) return Optional.of(startTime);
        return Optional.empty();
    }

    public void setStartTime(String startTime) {
        if (startTime == null) {
            this.startTime = null;
            return;
        }
        this.startTime = LocalDateTime.parse(startTime, Task.DATE_FORMATTER);
    }

    public Optional<LocalDateTime> getEndTime() {
       if (startTime != null && duration != null) return Optional.of(startTime.plus(duration));
       return Optional.empty();
    }
}
