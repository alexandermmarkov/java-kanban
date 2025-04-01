package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final static String CSV_HEADER = "id,type,name,status,description,epic";
    private final File file;

    private final Map<Integer, Task> allTasks = new TreeMap<>(new Comparator<>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    });

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("temp", ".txt");
        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(file);

        taskManager1.addTask(new Task("Задача1", "Тестовая задача #1"));
        taskManager1.addTask(new Task("Задача2", "Тестовая задача #2"));

        Epic epic = new Epic("Эпик1", "Тестовый Эпик #1");
        taskManager1.addEpic(epic);
        taskManager1.addEpic(new Epic("Эпик2", "Тестовый Эпик #2"));

        taskManager1.addSubtask(new Subtask("Подзадача1", "Тестовая подзадача #1", epic));
        taskManager1.addSubtask(new Subtask("Подзадача2", "Тестовая подзадача #2", epic));

        FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);

        System.out.println("В первом менеджере всего " + taskManager1.getAllTasks().size() + " задач.");
        System.out.println("Во втором менеджере всего " + taskManager2.getAllTasks().size() + " задач.");
        System.out.println(taskManager1.getAllTasks().equals(taskManager2.getAllTasks()) ? "Оба менеджера задач равны" : "Менеджеры задач отличаются");

        if (!file.delete()) {
            System.out.println("Возникли проблемы при удалении временного файла '" + file.getAbsolutePath() + "'");
        }
    }

    /// Задачи
    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void updateTask(int taskID, Task task) {
        super.updateTask(taskID, task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }
    /// ---------------------------------

    /// Эпики
    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateEpic(int epicID, Epic epic) {
        super.updateEpic(epicID, epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }
    /// ---------------------------------

    /// Подзадачи
    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(int subtaskID, Subtask subtask) {
        super.updateSubtask(subtaskID, subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }
    /// ---------------------------------

    public File getFile() {
        return file;
    }

    public void save() throws ManagerSaveException {
        uniteTasks();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            for (Task task : allTasks.values()) {
                writer.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка при записи данных в файл '" + file.getAbsolutePath() + "'.");
        }
    }

    public void uniteTasks() {
        allTasks.clear();
        allTasks.putAll(getTasksMap());
        allTasks.putAll(getEpicsMap());
        allTasks.putAll(getSubtasksMap());
    }

    public String toString(Task task) {
        int epicId = -1;
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            epicId = subtask.getEpic().getId();
        }
        return task.getId() + "," +
                task.getType() + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                (epicId >= 0 ? epicId : "");
    }

    public Task fromString(String value) {
        String[] taskData = value.split(",");
        int taskID = Integer.parseInt(taskData[0]);
        String taskType = taskData[1];
        Task task;

        if (taskType.equals(TaskType.TASK.name())) {
            task = new Task(taskData[2], taskData[4], taskID, taskData[3]);
        } else if (taskType.equals(TaskType.EPIC.name())) {
            task = new Epic(taskData[2], taskData[4], taskID, taskData[3]);
        } else {
            task = new Subtask(taskData[2], taskData[4], getEpicByID(Integer.parseInt(taskData[5])), taskID, taskData[3]);
        }
        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        if (!file.exists()) {
            System.out.println("Файл '" + file.getAbsolutePath() + "' не существует.");
            return taskManager;
        }

        try {
            String contents = Files.readString(file.toPath());
            if (contents.isBlank()) {
                System.out.println("Файл '" + file.getAbsolutePath() + "' пустой.");
                return taskManager;
            }
            String[] tasks = contents.split("\n");
            for (String taskLine : tasks) {
                if (taskLine.equals(CSV_HEADER)) {
                    continue;
                }
                Task task = taskManager.fromString(taskLine);
                switch (task.getType()) {
                    case TaskType.TASK:
                        taskManager.getTasksMap().put(task.getId(), task);
                        break;
                    case TaskType.EPIC:
                        taskManager.getEpicsMap().put(task.getId(), (Epic) task);
                        break;
                    default:
                        taskManager.getSubtasksMap().put(task.getId(), (Subtask) task);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Возникла ошибка при попытке загрузки менеджера задач из файла '" + file.getName() + "'");
        }
        return taskManager;
    }

    public Map<Integer, Task> getAllTasks() {
        uniteTasks();
        return allTasks;
    }

    public static class ManagerSaveException extends Error {
        public ManagerSaveException(String message) {
            super(message);
        }
    }
}
