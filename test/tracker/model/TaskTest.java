package tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;

import java.util.List;

class TaskTest {
    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        taskManager.addTask(task);
        final int taskId = task.getId();

        assertDoesNotThrow(() -> taskManager.getTaskByID(taskId), "Задача не найдена.");
        assertEquals(task, taskManager.getTaskByID(taskId), "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void updateTask() {
        Task task = new Task("Task", "Task Description");
        taskManager.addTask(task);
        taskManager.updateTask(task.getId(), new Task("UpdatedTask", "Updated Task Description",
                "IN_PROGRESS"));
        task = taskManager.getTaskByID(task.getId());
        assertEquals("UpdatedTask Updated Task Description IN_PROGRESS",
                task.getName() + " " + task.getDescription() + " " + task.getStatus(),
                "Задачи обновляются некорректно в Менеджере задач.");
    }

    @Test
    void deleteTask() {
        Task task = new Task("TaskToDelete", "Task To Delete Description");
        taskManager.addTask(task);
        taskManager.deleteTask(task.getId());
        assertThrows(NotFoundException.class, () -> taskManager.getTaskByID(task.getId()),
                "Удаление задач работает некорректно в Менеджере задач.");
    }
}