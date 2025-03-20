package tracker.model;

import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Epic{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "subtasks amount='" + subtasks.size() + "', "
                + "status='" + status
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
}
