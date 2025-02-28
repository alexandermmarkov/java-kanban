package tracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void resetTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldAddAndFindAllTypesOfTasks() {
        Task task = new Task("Test Task", "Test Task description");
        Epic epic = new Epic("Test Epic", "Test Epic description");
        Subtask subtask = new Subtask("Test Subtask", "Test Subtask description", epic);

        taskManager.addTask(task);
        final int taskId = task.getId();
        final Task savedTask = taskManager.getTaskByID(taskId);

        taskManager.addEpic(epic);
        final int epicId = epic.getId();
        final Epic savedEpic = taskManager.getEpicByID(epicId);

        taskManager.addSubtask(subtask);
        final int subtaskId = subtask.getId();
        final Subtask savedSubtask = taskManager.getSubtaskByID(subtaskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        assertNotNull(savedEpic, "Эпик не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
    }

    @Test
    void shouldAssignNewIdIfAlreadyUsed() {
        Task task1 = new Task("Test Task1", "Test Task1 description");
        Task task2 = new Task("Test Task2", "Test Task2 description");
        Task task3 = new Task("Test Task3", "Test Task3 description");
        Task task4 = new Task("Test Task4", "Test Task4 description");

        taskManager.addTask(task1);
        task2.setId(1);
        taskManager.addTask(task2);
        assertNotEquals(task1.getId(), task2.getId(), "Конфликт - у обеих задач ID = '" + task1.getId() + "'.");

        task3.setId(1);
        taskManager.addTask(task3);
        taskManager.addTask(task4);
        assertNotEquals(task3.getId(), task4.getId(), "Конфликт - у обеих задач ID = '" + task3.getId() + "'.");
    }

    @Test
    void shouldKeepTasksInManagerUnaltered() {
        Task task1 = new Task("Test Task1", "Test Task1 description");
        Task task2 = new Task("Test Task2", "Test Task2 description");

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        assertEquals(task1, taskManager.getTaskByID(task1.getId()), "Задача с ID = '" + task1.getId() + "' изменена после добавления в Менеджер задач.");
        assertEquals(task2, taskManager.getTaskByID(task2.getId()), "Задача с ID = '" + task2.getId() + "' изменена после добавления в Менеджер задач.");
    }

}