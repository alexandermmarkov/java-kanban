package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new HashMap<>();
    }

    public Epic(String name, String description, int id, String status) {
        super(name, description, id, status);
        subtasks = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Epic{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "subtasks amount='" + subtasks.size() + "', "
                + "status='" + status + "'"
                + (getDuration().isPresent() ? ", duration='" + duration.toMinutes() + "'" : "")
                + (getStartTime().isPresent() ? ", startTime='" + startTime.format(DATE_FORMATTER) + "'" : "")
                + (getEndTime().isPresent() ? ", endTime='" + endTime.format(DATE_FORMATTER) + "'" : "")
                + '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Task subtask) {
        if (!(subtask instanceof Subtask)) {
            return;
        }
        if (subtasks == null) {
            subtasks = new HashMap<>();
        }
        if (!subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), (Subtask) subtask);
        }
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    public void deleteSubtasks() {
        subtasks = new HashMap<>();
    }

    public void setEndTime(String endTime) {
        if (endTime == null) {
            this.endTime = null;
            return;
        }
        this.endTime = LocalDateTime.parse(endTime, Task.DATE_FORMATTER);
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        if (endTime != null) return Optional.of(endTime);
        return Optional.empty();
    }

    public void setDuration(long durationInMinutes) {
        duration = Duration.ofMinutes(durationInMinutes);
    }
}