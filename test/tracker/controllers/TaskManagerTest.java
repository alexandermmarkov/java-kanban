package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    public abstract void initializeTaskManager();

    protected Task createTask(int num) {
        return new Task("Test Task" + num, "Test Task" + num + " description");
    }

    protected Epic createEpic(int num) {
        return new Epic("Test Epic" + num, "Test Epic" + num + " description");
    }

    protected Subtask createSubtask(Epic epic, int num) {
        return new Subtask("Test Subtask" + num, "Test Subtask" + num + " description", epic);
    }
}
