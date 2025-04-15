package tracker.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static File file;

    @BeforeEach
    public void initializeTaskManager() {
        try {
            file = File.createTempFile("temp", ".txt");
            taskManager = FileBackedTaskManager.loadFromFile(file);
        } catch (IOException e) {
            System.out.println("Возникла ошибка при создании файла temp*.txt");
        }
    }

    @Test
    void shouldCreateEmptyFile() {
        try {
            String contents = Files.readString(file.toPath());
            String[] lines = contents.split("\n");
            assertTrue(file.exists() && (lines.length == 1) && (lines[0].isEmpty()));
        } catch (IOException e) {
            System.out.println("Возникла ошибка при чтении файла '" + file.getName()
                    + "' в тесте shouldCreateEmptyFile");
        }
    }

    @Test
    void shouldWriteTasksIntoFile() {
        try {
            Task task1 = createTask(1);
            Task task2 = createTask(2);
            taskManager.addTask(task1);
            taskManager.addTask(task2);

            Epic epic1 = createEpic(1);
            Epic epic2 = createEpic(2);
            taskManager.addEpic(epic1);
            taskManager.addEpic(epic2);

            Subtask subtask1 = createSubtask(epic1, 1);
            Subtask subtask2 = createSubtask(epic1, 2);
            Subtask subtask3 = createSubtask(epic1, 3);
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);
            taskManager.addSubtask(subtask3);

            String contents = Files.readString(file.toPath());
            String[] array = contents.split("\n");

            assertTrue((array.length == taskManager.getTasks().size() + taskManager.getEpics().size()
                    + taskManager.getSubtasks().size() + 1)
                    && array[0].equals("id,type,name,status,description,duration,startTime,endTime,epic"));
        } catch (IOException e) {
            System.out.println("Возникла ошибка при чтении файла '" + file.getName()
                    + "' в тесте shouldWriteTasksIntoFile");
        }
    }

    @Test
    void shouldImportTasksFromFile() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,duration,startTime,endTime,epic\n");
            writer.write("1,TASK,Задача1,NEW,Тестовая задача #1,60,14.04.2025 13:15,14.04.2025 14:15,\n");
            writer.write("4,EPIC,Эпик1,NEW,Тестовый Эпик #1,45,14.04.2025 13:45,14.04.2025 14:20,\n");
            writer.write("7,SUBTASK,Подзадача2,NEW,Тестовая подзадача #2,20,14.04.2025 14:00,14.04.2025 14:20,4\n");
        } catch (IOException e) {
            System.out.println("Возникла ошибка при открытии файла '" + file.getName()
                    + "' в тесте shouldImportTasksFromFile");
        }
        taskManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(3, taskManager.getPrioritizedTasks().size());
    }

    @Test
    void shouldThrowOnIntersection() {
        assertThrows(FileBackedTaskManager.TasksIntersectionException.class, () -> {
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
        assertThrows(FileBackedTaskManager.TasksIntersectionException.class, () -> {
                    Task task1 = new Task("Задача1", "Тестовая задача #1",
                            LocalDateTime.now().format(Task.DATE_FORMATTER), 50);
                    Task task2 = new Task("Задача2", "Тестовая задача #2",
                            task1.getStartTime().get().plusMonths(14).format(Task.DATE_FORMATTER), 30);
                    taskManager.addTask(task1);
                    taskManager.addTask(task2);
                }, "'Задача2' не выходит за границу максимального времени планирования задач"
        );
    }

    @AfterEach
    void deleteTempFile() throws FileSystemException {
        if (!file.delete()) {
            throw new FileSystemException("Не удалось удалить временный файл '" + file.getAbsolutePath() + "'");
        }
    }
}
