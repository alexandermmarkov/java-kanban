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

    @Override
    public String toString() {
        return "Subtask{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "epic=" + epic + "', "
                + "status='" + status
                + '}';
    }

    public Epic getEpic() {
        return epic;
    }
}
