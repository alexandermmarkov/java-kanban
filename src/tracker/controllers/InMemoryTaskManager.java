package tracker.controllers;

import tracker.history.HistoryManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    /// Задачи
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearTasks() {
        tasks = new HashMap<>();
    }

    @Override
    public Task getTaskByID(int taskID) {
        Task task = tasks.getOrDefault(taskID, null);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        int id = Task.getNewIdentificator();
        task.setId(id);
        if (!tasks.containsKey(id)) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateTask(int taskID, Task task) {
        if ((task != null) && (tasks.containsKey(taskID))) {
            task.setId(taskID);
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }
    /// ---------------------------------

    /// Эпики
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    @Override
    public Epic getEpicByID(int epicID) {
        Epic epic = epics.getOrDefault(epicID, null);
        if (epic != null) {
            historyManager.add(epic);
        }
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
    }

    @Override
    public void updateEpic(int epicID, Epic epic) {
        if ((epic != null) && (epics.containsKey(epicID))) {
            epic.setId(epicID);
            updateEpicStatus(epic);
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = getEpicByID(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks().values()) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(id);
        }
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
    public HashMap<Integer, Subtask> getSubtasksOfEpic(int epicID) {
        Epic epic = getEpicByID(epicID);
        if (epic == null) {
            return null;
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
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.deleteSubtasks();
            updateEpicStatus(epic);
        }
        subtasks = new HashMap<>();
    }

    @Override
    public Subtask getSubtaskByID(int subtaskID) {
        Subtask subtask = subtasks.getOrDefault(subtaskID, null);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
        int id = Task.getNewIdentificator();
        subtask.setId(id);
        Epic epic = subtask.getEpic();
        if (!subtasks.containsKey(id) && (epic != null)) {
            epic.addSubtask(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpic());
            subtasks.put(subtask.getId(), subtask);
        }
    }

    @Override
    public void updateSubtask(int subtaskID, Subtask subtask) {
        if ((subtask != null) && (subtasks.containsKey(subtaskID))) {
            subtask.setId(subtaskID);
            subtask.getEpic().getSubtasks().put(subtaskID, subtask);
            updateEpicStatus(subtask.getEpic());
            subtasks.put(subtask.getId(), subtask);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = getSubtaskByID(id);
        if (subtask != null) {
            Epic epic = subtask.getEpic();
            epic.deleteSubtask(id);
            updateEpicStatus(epic);
            subtasks.remove(id);
        }
    }

    /// ---------------------------------

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }
}
