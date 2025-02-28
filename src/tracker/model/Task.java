package tracker.model;

import java.util.ArrayList;
import java.util.Objects;

public class Task {
    protected static int identificator;
    public static ArrayList<Integer> idToIgnoreList = new ArrayList<>();
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected int id;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public Task(String name, String description, int id) {
        if ((id != 0) && !idToIgnoreList.contains(id)) {
            idToIgnoreList.add(id);
            this.id = id;
        }
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
        if ((id != 0) && !idToIgnoreList.contains(id)) {
            this.id = id;
        }
        this.name = name;
        this.description = description;
        this.status = TaskStatus.valueOf(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, status, id);
    }

    @Override
    public String toString() {
        return "Task{"
                + "id='" + id + "', "
                + "name='" + name + "', "
                + "description='" + description + "', "
                + "status='" + status + "', "
                + '}';
    }

    public static int getNewIdentificator() {
        int newId = ++identificator;
        while (idToIgnoreList.contains(newId)) {
            newId++;
        }
        return newId;
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
        if (idToIgnoreList.contains(id)) {
            id = getNewIdentificator();
        } else {
            idToIgnoreList.add(id);
        }
        this.id = id;
    }
}
