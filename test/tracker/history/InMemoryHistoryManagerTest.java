package tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    void initializeTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldKeepTasksInHistoryUnaltered() {
        Task task1 = new Task("Test Task1", "Test Task1 Description");
        Task task2 = new Task("Test Task2", "Test Task2 Description");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        taskManager.updateTask(task1.getId(), new Task("Updated Test Task1", "Updated Test Task1 Description"));
        taskManager.updateTask(task2.getId(), new Task("Updated Test Task2", "Updated Test Task2 Description"));
        assertEquals(task1, taskManager.getHistory().get(0), "Задача с ID = '" + task1.getId() + "' изменена в Истории просмотров.");
        assertEquals(task2, taskManager.getHistory().get(1), "Задача с ID = '" + task2.getId() + "' изменена в Истории просмотров.");
    }

    @Test
    void shouldAddTaskProperly() {
        Task task1 = new Task("Test Task1", "Test Task1 Description");
        Task task2 = new Task("Test Task2", "Test Task2 Description");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        assertEquals(task2.getId(), taskManager.getHistory().getLast().getId(), "Добавление задач в историю просмотров работает некорректно");
    }

    @Test
    void shouldDeleteTaskProperly() {
        Task task1 = new Task("Test Task1", "Test Task1 Description");
        Task task2 = new Task("Test Task2", "Test Task2 Description");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        taskManager.getTaskByID(task1.getId());
        assertTrue((taskManager.getHistory().size() == 2) && (taskManager.getHistory().getLast().getId() == task1.getId()), "Удаление задач из истории просмотров работает некорректно");
    }
}