package tracker.controllers;

import tracker.exceptions.NotFoundException;
import tracker.exceptions.TasksIntersectionException;
import tracker.history.HistoryManager;
import tracker.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;
    private final Map<Integer, Task> allTasks;
    private final Map<LocalDateTime, Integer> taskIntervals;
    private final Set<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
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

    /// Задачи
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Map<Integer, Task> getTasksMap() {
        return new HashMap<>(tasks);
    }

    protected void setTasksMap(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void clearTasks() {
        tasks = new HashMap<>();
        List<Task> tasks = historyManager.getHistory();
        tasks.stream()
                .filter(task -> task.getType() == TaskType.TASK)
                .forEach(task -> historyManager.remove(task.getId()));
        uniteTasks();
    }

    @Override
    public Task getTaskByID(int taskID) throws NotFoundException {
        Task task = Optional.ofNullable(tasks.get(taskID))
                .orElseThrow(() -> new NotFoundException("Задача с ID='" + taskID + "' не найдена."));
        historyManager.add(task);
        return task;
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        int id = Task.getNewIdentificator();
        task.setId(id);
        releaseTaskIntervals(task);
        if (isNotIntersect(task)) {
            if (!tasks.containsKey(id)) {
                tasks.put(task.getId(), task);
            }
            uniteTasks();
        }
    }

    @Override
    public void updateTask(int taskID, Task task) throws NotFoundException {
        if (!tasks.containsKey(taskID)) {
            throw new NotFoundException("Задача с ID='" + taskID + "' не найдена.");
        }
        task.setId(taskID);
        releaseTaskIntervals(task);
        if (isNotIntersect(task)) {
            tasks.put(task.getId(), task);
            uniteTasks();
        }
    }

    @Override
    public void deleteTask(int id) throws NotFoundException {
        Task task = getTaskByID(id);
        if (task == null) {
            throw new NotFoundException("Задача с ID='" + id + "' не найдена.");
        }
        releaseTaskIntervals(task);
        tasks.remove(id);
        historyManager.remove(id);
        uniteTasks();
    }
    /// ---------------------------------

    /// Эпики
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Map<Integer, Epic> getEpicsMap() {
        return new HashMap<>(epics);
    }

    protected void setEpicsMap(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void clearEpics() {
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        List<Task> epics = historyManager.getHistory();
        epics.stream()
                .filter(epic -> epic.getType() == TaskType.EPIC)
                .forEach(epic -> historyManager.remove(epic.getId()));
        uniteTasks();
    }

    @Override
    public Epic getEpicByID(int epicID) throws NotFoundException {
        Epic epic = Optional.ofNullable(epics.get(epicID))
                .orElseThrow(() -> new NotFoundException("Эпик с ID='" + epicID + "' не найден."));
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        int id = Task.getNewIdentificator();
        epic.setId(id);
        if (!epics.containsKey(id)) {
            epics.put(epic.getId(), epic);
        }
        uniteTasks();
    }

    @Override
    public void updateEpic(int epicID, Epic epic) {
        if ((epic != null) && (epics.containsKey(epicID))) {
            epic.setId(epicID);
            updateEpicStatus(epic);
            epics.put(epic.getId(), epic);
            uniteTasks();
        }
    }

    @Override
    public void deleteEpic(int id) throws NotFoundException {
        Epic epic = getEpicByID(id);
        if (epic == null) {
            throw new NotFoundException("Эпик с ID='" + id + "' не найден.");
        }
        if (epic.getSubtasks() != null) {
            epic.getSubtasks().values().stream()
                    .forEach(this::releaseTaskIntervals);
            epic.getSubtasks().values().stream()
                    .forEach(subtask -> {
                        subtasks.remove(subtask.getId());
                        historyManager.remove(subtask.getId());
                    });
        }
        epics.remove(id);
        historyManager.remove(id);
        uniteTasks();
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        int newSubtasks = 0;
        int doneSubtasks = 0;
        for (Subtask subtask : epic.getSubtasks().values()) {
            if (subtask.getStatus().equals(TaskStatus.NEW)) {
                newSubtasks++;
            } else if (subtask.getStatus().equals(TaskStatus.DONE)) {
                doneSubtasks++;
            }
        }
        if (epic.getSubtasks().isEmpty() || newSubtasks == epic.getSubtasks().size()) {
            epic.setStatus("NEW");
        } else if (doneSubtasks == epic.getSubtasks().size()) {
            epic.setStatus("DONE");
        } else {
            epic.setStatus("IN_PROGRESS");
        }
    }

    @Override
    public void updateEpicTime(Epic epic) {
        if (epic.getSubtasks().isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(0);
            return;
        }
        Optional<LocalDateTime> startDateTime = epic.getSubtasks().values().stream()
                .filter(subtask -> subtask.getStartTime().isPresent() && subtask.getEndTime().isPresent())
                .map(subtask -> subtask.getStartTime().get())
                .min((startTime1, startTime2) -> {
                    if (startTime1.isAfter(startTime2)) {
                        return 1;
                    } else if (startTime1.isBefore(startTime2)) {
                        return -1;
                    }
                    return 0;
                });
        Optional<LocalDateTime> endDateTime = epic.getSubtasks().values().stream()
                .filter(subtask -> subtask.getStartTime().isPresent() && subtask.getEndTime().isPresent())
                .map(subtask -> subtask.getEndTime().get())
                .max((endTime1, endTime2) -> {
                    if (endTime1.isAfter(endTime2)) {
                        return 1;
                    } else if (endTime1.isBefore(endTime2)) {
                        return -1;
                    }
                    return 0;
                });

        startDateTime.ifPresent(localDateTime
                -> epic.setStartTime(localDateTime.format(Task.DATE_FORMATTER)));
        endDateTime.ifPresent(localDateTime -> epic.setEndTime(localDateTime.format(Task.DATE_FORMATTER)));

        Duration duration = Duration.ZERO;
        for (Subtask subtask : epic.getSubtasks().values()) {
            if (subtask.getDuration().isEmpty()) continue;
            duration = duration.plus(subtask.getDuration().get());
        }
        epic.setDuration(duration.toMinutes());
    }

    @Override
    public HashMap<Integer, Subtask> getEpicSubtasks(int epicID) throws NotFoundException {
        Epic epic = epics.get(epicID);
        if (epic == null) {
            throw new NotFoundException("Эпик с ID='" + epicID + "' не найден.");
        }
        return epic.getSubtasks();
    }
    /// ---------------------------------

    /// Подзадачи
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Map<Integer, Subtask> getSubtasksMap() {
        return new HashMap<>(subtasks);
    }

    protected void setSubtasks(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public void clearSubtasks() {
        epics.values().stream()
                .forEach(epic -> {
                    epic.deleteSubtasks();
                    updateEpicStatus(epic);
                });
        subtasks = new HashMap<>();
        List<Task> subTasks = historyManager.getHistory();
        subTasks.stream()
                .filter(subTask -> subTask.getType() == TaskType.SUBTASK)
                .forEach(subTask -> historyManager.remove(subTask.getId()));
        uniteTasks();
    }

    @Override
    public Subtask getSubtaskByID(int subtaskID) throws NotFoundException {
        Subtask subtask = Optional.ofNullable(subtasks.get(subtaskID))
                .orElseThrow(() -> new NotFoundException("Подзадача с ID='" + subtaskID + "' не найдена."));
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
        int id = Task.getNewIdentificator();
        subtask.setId(id);
        releaseTaskIntervals(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (!subtasks.containsKey(id) && (epic != null) && isNotIntersect(subtask)) {
            epic.addSubtask(subtask);
            updateEpicStatus(epic);
            updateEpicTime(epic);
            subtasks.put(subtask.getId(), subtask);
            uniteTasks();
        }
    }

    @Override
    public void updateSubtask(int subtaskID, Subtask subtask) {
        if (subtask == null) return;
        subtask.setId(subtaskID);
        releaseTaskIntervals(subtask);
        if (subtasks.containsKey(subtaskID) && isNotIntersect(subtask)) {
            Epic epic = epics.get(subtask.getEpicId());
            epic.getSubtasks().put(subtaskID, subtask);
            updateEpicStatus(epic);
            subtasks.put(subtask.getId(), subtask);
            uniteTasks();
        }
    }

    @Override
    public void deleteSubtask(int id) throws NotFoundException {
        Subtask subtask = getSubtaskByID(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача с ID='" + id + "' не найдена.");
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.deleteSubtask(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        releaseTaskIntervals(subtask);
        subtasks.remove(id);
        historyManager.remove(id);
        uniteTasks();
    }

    /// ---------------------------------

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public void uniteTasks() {
        allTasks.clear();
        allTasks.putAll(getTasksMap());
        allTasks.putAll(getEpicsMap());
        allTasks.putAll(getSubtasksMap());
        prioritizedTasks.clear();
        allTasks.values().stream()
                .filter(task -> task.getStartTime().isPresent())
                .forEach(prioritizedTasks::add);
    }

    public Map<Integer, Task> getAllTasks() {
        return allTasks;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
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
                taskIntervals.put(dateTime, 0);
                dateTime = dateTime.plusMinutes(15);
            }
            setTaskIntervals(taskToCheck.getId(), taskToCheck.getStartTime().get(), taskToCheck.getEndTime().get());
            return true;
        }
        if (taskIntervals.keySet().stream().toList().getLast().isBefore(taskToCheck.getEndTime().get())) {
            throw new TasksIntersectionException("Задача '" + taskToCheck.getName() + "'" +
                    " выходит за границу максимального времени планирования задач "
                    + taskIntervals.keySet().stream().toList().getLast().format(Task.DATE_FORMATTER));
        }
        while (dateTime.isBefore(taskToCheck.getEndTime().get())) {
            int taskID = taskIntervals.getOrDefault(dateTime, 0);
            if ((taskID > 0) && (taskID != taskToCheck.getId())) {
                throw new TasksIntersectionException("Задача с ID='" + taskToCheck.getId() + "'" +
                        " пересекается по времени выполнения с задачей с ID='" + taskID + "'");
            }
            dateTime = dateTime.plusMinutes(15);
        }
        setTaskIntervals(taskToCheck.getId(), taskToCheck.getStartTime().get(), taskToCheck.getEndTime().get());
        return true;
    }

    @Override
    public void setTaskIntervals(Integer taskID, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime occupiedTime = startTime;
        while (occupiedTime.isBefore(endTime)) {
            taskIntervals.put(occupiedTime, taskID);
            occupiedTime = occupiedTime.plusMinutes(15);
        }
    }

    @Override
    public Map<LocalDateTime, Integer> getTaskIntervals() {
        return Map.copyOf(taskIntervals);
    }

    @Override
    public void releaseTaskIntervals(Task task) {
        List<LocalDateTime> dtList = new ArrayList<>();
        for (Map.Entry<LocalDateTime, Integer> entry : taskIntervals.entrySet()) {
            if (entry.getValue() == task.getId()) {
                dtList.add(entry.getKey());
            }
        }
        for (LocalDateTime localDateTime : dtList) {
            taskIntervals.remove(localDateTime);
        }
    }
}