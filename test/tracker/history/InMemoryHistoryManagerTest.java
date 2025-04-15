package tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManagerTest;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void initializeTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldNotBeTasksInHistoryByDefault() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        Task task3 = createTask(3);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотров изначально не пустая.");
    }

    @Test
    void shouldKeepTasksInHistoryUnaltered() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        taskManager.updateTask(task1.getId(),
                new Task("Updated Test Task1", "Updated Test Task1 Description"));
        taskManager.updateTask(task2.getId(),
                new Task("Updated Test Task2", "Updated Test Task2 Description"));
        assertEquals(task1, taskManager.getHistory().get(0),
                "Задача с ID = '" + task1.getId() + "' изменена в Истории просмотров.");
        assertEquals(task2, taskManager.getHistory().get(1),
                "Задача с ID = '" + task2.getId() + "' изменена в Истории просмотров.");
    }

    @Test
    void shouldAddTaskProperly() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        assertEquals(task2.getId(), taskManager.getHistory().getLast().getId(),
                "Добавление задач в историю просмотров работает некорректно");
    }

    @Test
    void shouldDeleteTaskProperly() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        Task task3 = createTask(3);
        Task task4 = createTask(4);
        Task task5 = createTask(5);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.addTask(task4);
        taskManager.addTask(task5);
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task3.getId());
        taskManager.getTaskByID(task4.getId());
        taskManager.getTaskByID(task3.getId());
        taskManager.getTaskByID(task5.getId());
        taskManager.getTaskByID(task1.getId());
        assertTrue((taskManager.getHistory().size() == 5)
                        && (taskManager.getHistory().getFirst().getId() == task2.getId())
                        && (taskManager.getHistory().get(2).getId() == task3.getId())
                        && (taskManager.getHistory().getLast().getId() == task1.getId()),
                "Удаление задач из истории просмотров работает некорректно");
    }
}