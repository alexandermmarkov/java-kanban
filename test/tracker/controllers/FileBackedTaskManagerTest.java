package tracker.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {
    private static FileBackedTaskManager taskManager;
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
            System.out.println("Возникла ошибка при чтении файла '" + file.getName() + "' в тесте shouldCreateEmptyFile");
        }
    }

    @Test
    void shouldWriteTasksIntoFile() {
        try {
            Task task1 = new Task("Задача1", "Тестовая задача #1");
            Task task2 = new Task("Задача2", "Тестовая задача #2");
            taskManager.addTask(task1);
            taskManager.addTask(task2);

            Epic epic1 = new Epic("Эпик1", "Тестовый Эпик #1");
            Epic epic2 = new Epic("Эпик2", "Тестовый Эпик #2");
            taskManager.addEpic(epic1);
            taskManager.addEpic(epic2);

            Subtask subtask1 = new Subtask("Подзадача1", "Тестовая подзадача #1", epic1);
            Subtask subtask2 = new Subtask("Подзадача2", "Тестовая подзадача #2", epic1);
            Subtask subtask3 = new Subtask("Подзадача3", "Тестовая подзадача #3", epic1);
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);
            taskManager.addSubtask(subtask3);

            String contents = Files.readString(file.toPath());
            String[] array = contents.split("\n");

            assertTrue((array.length == taskManager.getTasks().size() + taskManager.getEpics().size()
                    + taskManager.getSubtasks().size() + 1)
                    && array[0].equals("id,type,name,status,description,epic"));
        } catch (IOException e) {
            System.out.println("Возникла ошибка при чтении файла '" + file.getName() + "' в тесте shouldWriteTasksIntoFile");
        }
    }

    @Test
    void shouldImportTasksFromFile() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");
            writer.write("1,TASK,Задача1,NEW,Тестовая задача #1,\n");
            writer.write("2,EPIC,Эпик1,NEW,Тестовый Эпик #1,\n");
            writer.write("3,SUBTASK,Подзадача1,NEW,Тестовая подзадача #1,2\n");
        } catch (IOException e) {
            System.out.println("Возникла ошибка при открытии файла '" + file.getName() + "' в тесте shouldImportTasksFromFile");
        }
        taskManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(3, taskManager.getAllTasks().size());
    }

    @AfterEach
    void deleteTempFile() throws FileSystemException {
        if (!file.delete()) {
            throw new FileSystemException("Не удалось удалить временный файл '" + file.getAbsolutePath() + "'");
        }
    }
}
