package tracker.controllers;

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
    private final Map<Integer, Task> allTasks;
    private final Map<LocalDateTime, Boolean> taskIntervals;
    private final Set<Task> prioritizedTasks;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
        allTasks = new TreeMap<>((o1, o2) -> o1 - o2);
        taskIntervals = new HashMap<>();
        prioritizedTasks = new TreeSet<>((o1, o2) -> {
            if (o1.getStartTime().get().isAfter(o2.getStartTime().get())) {
                return 1;
            } else if (o1.getStartTime().get().isBefore(o2.getStartTime().get())) {
                return -1;
            }
            return o1.getId() - o2.getId();
        });
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

        taskManager1.addSubtask(new Subtask("Подзадача1", "Тестовая подзадача #1", epic,
                LocalDateTime.now().plusMinutes(210).format(Task.DATE_FORMATTER), 15));
        taskManager1.addSubtask(new Subtask("Подзадача2", "Тестовая подзадача #2", epic,
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
        if (isNotIntersect(task)) {
            super.addTask(task);
            save();
        }
    }

    @Override
    public void updateTask(int taskID, Task task) {
        if (isNotIntersect(task)) {
            super.updateTask(taskID, task);
            save();
        }
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
        if (isNotIntersect(subtask)) {
            super.addSubtask(subtask);
            save();
        }
    }

    @Override
    public void updateSubtask(int subtaskID, Subtask subtask) {
        if (isNotIntersect(subtask)) {
            super.updateSubtask(subtaskID, subtask);
            save();
        }
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
        uniteTasks();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            for (Task task : allTasks.values()) {
                writer.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка при записи данных в файл '" + file.getAbsolutePath()
                    + "': " + e.getMessage());
        }
    }

    private void uniteTasks() {
        allTasks.clear();
        allTasks.putAll(getTasksMap());
        allTasks.putAll(getEpicsMap());
        allTasks.putAll(getSubtasksMap());
        prioritizedTasks.clear();
        allTasks.values().stream()
                .filter(task -> task.getStartTime().isPresent())
                .forEach(prioritizedTasks::add);
    }

    private String toString(Task task) {
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
                        getEpicsMap().get(Integer.parseInt(taskData[8])), taskID,
                        taskData[3]) :
                        new Subtask(taskData[2], taskData[4], getEpicsMap().get(Integer.parseInt(taskData[8])), taskID,
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
                    .filter(subtask -> taskManager.getEpicsMap().containsKey(subtask.getEpic().getId()))
                    .map(subtask -> {
                        subtask.getEpic().addSubtask(subtask);
                        return subtask.getEpic();
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

    public Map<Integer, Task> getAllTasks() {
        uniteTasks();
        return allTasks;
    }

    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    public boolean isNotIntersect(Task taskToCheck) {
        if (taskToCheck.getStartTime().isEmpty()) return true;
        // Оригинальная реализация без проверки новой таблицы интервалов
        /*uniteTasks();
        Optional<Task> foundTask = prioritizedTasks.stream()
                .filter(task -> !task.getType().equals(TaskType.EPIC))
                .filter(task -> (taskToCheck.getStartTime().get().isBefore(task.getEndTime().get()) &&
                        taskToCheck.getStartTime().get().isAfter(task.getStartTime().get())) ||
                        (taskToCheck.getStartTime().get().isBefore(task.getStartTime().get()) &&
                                taskToCheck.getEndTime().get().isAfter(task.getStartTime().get()))
                ).findFirst();
        if (foundTask.isPresent()) {
            System.out.println();
            throw new TasksIntercetionException("Задача '" + taskToCheck.getName()
                    + "' пересекается с " + (foundTask.get().getType() == TaskType.TASK ? "задачей" : "подзадачей")
                    + " '" + foundTask.get().getName() + "'");
        }*/
        LocalDateTime dateTime = taskToCheck.getStartTime().get();
        if (taskIntervals.isEmpty()) {
            while (dateTime.isBefore(taskToCheck.getStartTime().get().plusYears(1))) {
                taskIntervals.put(dateTime, false);
                dateTime = dateTime.plusMinutes(15);
            }
            setTaskIntervals(taskToCheck.getStartTime().get(), taskToCheck.getEndTime().get());
            return true;
        }
        if (taskIntervals.keySet().stream().toList().getLast().isBefore(taskToCheck.getEndTime().get())) {
            throw new TasksIntersectionException("Задача '" + taskToCheck.getName() + "'" +
                    " выходит за границу максимального времени планирования задач "
                    + taskIntervals.keySet().stream().toList().getLast().format(Task.DATE_FORMATTER));
        }
        while (dateTime.isBefore(taskToCheck.getEndTime().get())) {
            if (taskIntervals.getOrDefault(dateTime, false)) {
                throw new TasksIntersectionException("Задача '" + taskToCheck.getName() + "'" +
                        " пересекается по веремени выполнения с другой задачей");
            }
            dateTime = dateTime.plusMinutes(15);
        }
        setTaskIntervals(taskToCheck.getStartTime().get(), taskToCheck.getEndTime().get());
        return true;
    }

    protected void setTaskIntervals(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime occupiedTime = startTime;
        while (occupiedTime.isBefore(endTime)) {
            taskIntervals.put(occupiedTime, true);
            occupiedTime = occupiedTime.plusMinutes(15);
        }
    }

    public Map<LocalDateTime, Boolean> getTaskIntervals() {
        return Map.copyOf(taskIntervals);
    }

    public static class ManagerSaveException extends Error {
        public ManagerSaveException(String message) {
            super(message);
        }
    }

    public static class TasksIntersectionException extends Error {
        public TasksIntersectionException(String message) {
            super(message);
        }
    }
}
