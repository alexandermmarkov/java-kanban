import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasks;

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

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(int id, Subtask subtask) {
        if ((subtask != null) && (!subtasks.containsKey(id))) {
            subtasks.put(id, subtask);
        }
    }
}
