package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String CSV_HEADER = "id,type,name,status,description,duration,startTime,endTime,epic";
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("temp", ".txt");
        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(file);

        taskManager1.addTask(new Task("Задача1", "Тестовая задача #1",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 120));
        taskManager1.addTask(new Task("Задача2", "Тестовая задача #2",
                LocalDateTime.now().plusMinutes(120).format(Task.DATE_FORMATTER), 90));

        Epic epic = new Epic("Эпик1", "Тестовый Эпик #1");
        taskManager1.addEpic(epic);
        taskManager1.addEpic(new Epic("Эпик2", "Тестовый Эпик #2"));

        taskManager1.addSubtask(new Subtask("Подзадача1", "Тестовая подзадача #1", epic.getId(),
                LocalDateTime.now().plusMinutes(210).format(Task.DATE_FORMATTER), 15));
        taskManager1.addSubtask(new Subtask("Подзадача2", "Тестовая подзадача #2", epic.getId(),
                LocalDateTime.now().plusMinutes(225).format(Task.DATE_FORMATTER), 30));

        FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);

        System.out.println("В первом менеджере всего " + taskManager1.getPrioritizedTasks().size() + " задач.");
        System.out.println("Во втором менеджере всего " + taskManager2.getPrioritizedTasks().size() + " задач.");
        System.out.println(taskManager1.getPrioritizedTasks().equals(taskManager2.getPrioritizedTasks())
                ? "Оба менеджера задач равны." : "Менеджеры задач отличаются.");

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

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            for (Task task : getAllTasks().values()) {
                writer.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка при записи данных в файл '" + file.getAbsolutePath()
                    + "': " + e.getMessage());
        }
    }

    private String toString(Task task) {
        int epicId = -1;
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            epicId = subtask.getEpicId();
        }
        return task.getId() + "," +
                task.getType() + "," +
                task.getName() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                (task.getDuration().isPresent() ? task.getDuration().get().toMinutes() : " ") + "," +
                (task.getStartTime().isPresent() ? task.getStartTime().get().format(Task.DATE_FORMATTER) : " ") + "," +
                (task.getEndTime().isPresent() ? task.getEndTime().get().format(Task.DATE_FORMATTER) : " ") + "," +
                (epicId >= 0 ? epicId : "");
    }

    private Task fromString(String value) {
        String[] taskData = value.split(",");
        int taskID = Integer.parseInt(taskData[0]);
        String taskType = taskData[1];
        Task task;

        switch (TaskType.valueOf(taskType)) {
            case TASK:
                task = (taskData[6].isBlank() ? new Task(taskData[2], taskData[4], taskID, taskData[3]) :
                        new Task(taskData[2], taskData[4], taskID, taskData[3], taskData[6],
                                Integer.parseInt(taskData[5])));
                break;
            case EPIC:
                task = new Epic(taskData[2], taskData[4], taskID, taskData[3]);
                break;
            default:
                task = (taskData[6].isBlank() ? new Subtask(taskData[2], taskData[4],
                        Integer.parseInt(taskData[8]), taskID,
                        taskData[3]) :
                        new Subtask(taskData[2], taskData[4], Integer.parseInt(taskData[8]), taskID,
                                taskData[3], taskData[6], Integer.parseInt(taskData[5])));
                break;
        }
        return task;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);

        try {
            String contents = Files.readString(file.toPath());
            if (contents.isBlank()) {
                return taskManager;
            }

            String[] tasks = contents.split("\n");
            Arrays.asList(tasks).stream()
                    .filter(taskLine -> !taskLine.equals(CSV_HEADER))
                    .forEach(taskLine -> {
                        switch (taskManager.fromString(taskLine).getType()) {
                            case TaskType.TASK:
                                taskManager.setTasksMap(taskManager.fromString(taskLine));
                                break;
                            case EPIC:
                                taskManager.setEpicsMap((Epic) taskManager.fromString(taskLine));
                                break;
                            default:
                                taskManager.setSubtasks((Subtask) taskManager.fromString(taskLine));
                                break;
                        }
                    });

            taskManager.getSubtasksMap().values().stream()
                    .filter(subtask -> taskManager.getEpicsMap().containsKey(subtask.getEpicId()))
                    .map(subtask -> {
                        taskManager.getEpicsMap().get(subtask.getEpicId()).addSubtask(subtask);
                        return taskManager.getEpicsMap().get(subtask.getEpicId());
                    })
                    .toList()
                    .forEach(taskManager::updateEpicTime);

            taskManager.uniteTasks();
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка при попытке загрузки менеджера задач из файла '"
                    + file.getAbsolutePath() + "': " + e.getMessage());
        }
        return taskManager;
    }
}
