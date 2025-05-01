package tracker.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.exceptions.TasksIntersectionException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        return new Subtask("Test Subtask" + num, "Test Subtask" + num + " description", epic.getId());
    }

    @Test
    void shouldThrowOnIntersection() {
        assertThrows(TasksIntersectionException.class, () -> {
                    Task task1 = new Task("Задача1", "Тестовая задача #1",
                            LocalDateTime.now().format(Task.DATE_FORMATTER), 50);
                    Task task2 = new Task("Задача2", "Тестовая задача #2",
                            task1.getStartTime().get().plusMinutes(15).format(Task.DATE_FORMATTER), 30);
                    taskManager.addTask(task1);
                    taskManager.addTask(task2);
                }, "'Задача1' не пересекается с задачей 'Задача2'"
        );
    }

    @Test
    void shouldNotThrowWithoutIntersection() {
        Assertions.assertDoesNotThrow(() -> {
            Task task1 = new Task("Задача1", "Тестовая задача #1",
                    LocalDateTime.now().minusMinutes(90).format(Task.DATE_FORMATTER), 80);
            Task task2 = new Task("Задача2", "Тестовая задача #2",
                    LocalDateTime.now().format(Task.DATE_FORMATTER), 30);
            taskManager.addTask(task1);
            taskManager.addTask(task2);
        }, "'Задача1' пересекается с задачей 'Задача2'");
    }

    @Test
    void shouldThrowWhenBeyond1YearLimit() {
        assertThrows(TasksIntersectionException.class, () -> {
                    Task task1 = new Task("Задача1", "Тестовая задача #1",
                            LocalDateTime.now().format(Task.DATE_FORMATTER), 50);
                    Task task2 = new Task("Задача2", "Тестовая задача #2",
                            task1.getStartTime().get().plusMonths(14).format(Task.DATE_FORMATTER), 30);
                    taskManager.addTask(task1);
                    taskManager.addTask(task2);
                }, "'Задача2' не выходит за границу максимального времени планирования задач"
        );
    }

    @Test
    void shouldDeleteFromPrioritizedTasks() {
        Task task1 = createTask(1);
        task1.setStartTime(LocalDateTime.now().format(Task.DATE_FORMATTER));
        task1.setDuration(30);
        Task task2 = createTask(2);
        task2.setStartTime(task1.getEndTime().get().format(Task.DATE_FORMATTER));
        task2.setDuration(30);
        Task task3 = createTask(3);
        task3.setStartTime(task2.getEndTime().get().format(Task.DATE_FORMATTER));
        task3.setDuration(30);
        Task task4 = createTask(4);
        task4.setStartTime(task3.getEndTime().get().format(Task.DATE_FORMATTER));
        task4.setDuration(30);

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.addTask(task4);

        taskManager.deleteTask(task4.getId());
        assertTrue(taskManager.getPrioritizedTasks().size() == 3
                        && taskManager.getPrioritizedTasks().getLast().getId() == task3.getId(),
                "Удаление задач из отсортированного по времени списка работает некорректно.");
    }
}