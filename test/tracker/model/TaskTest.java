package tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

import java.util.List;
import java.util.Optional;

class TaskTest {
    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        taskManager.addTask(task);
        final int taskId = task.getId();

        final Optional<Task> savedTask = taskManager.getTaskByID(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask.get(), "Задачи не совпадают.");

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
        task = taskManager.getTaskByID(task.getId()).get();
        assertEquals("UpdatedTask Updated Task Description IN_PROGRESS",
                task.getName() + " " + task.getDescription() + " " + task.getStatus(),
                "Задачи обновляются некорректно в Менеджере задач.");
    }

    @Test
    void deleteTask() {
        Task task = new Task("TaskToDelete", "Task To Delete Description");
        taskManager.addTask(task);
        taskManager.deleteTask(task.getId());
        assertTrue(taskManager.getTaskByID(task.getId()).isEmpty(),
                "Удаление задач работает некорректно в Менеджере задач.");
    }

}