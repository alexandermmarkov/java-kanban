package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int identificator;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public static int getNewIdentificator() {
        return ++identificator;
    }

    /// Задачи
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void clearTasks() {
        tasks = new HashMap<>();
    }

    public Task getTaskByID(int taskID) {
        return tasks.getOrDefault(taskID, null);
    }

    public void addTask(Task task) {
        int newId = getNewIdentificator();
        if ((task != null) && (!tasks.containsKey(newId))) {
            task.setId(newId);
            tasks.put(task.getId(), task);
        }
    }

    public void updateTask(int id, Task task) {
        if ((task != null) && (tasks.containsKey(id))) {
            task.setId(id);
            tasks.put(task.getId(), task);
        }
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }
    /// ---------------------------------

    /// Эпики
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void clearEpics() {
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public Epic getEpicByID(int epicID) {
        return epics.getOrDefault(epicID, null);
    }

    public void addEpic(Epic epic) {
        int newId = getNewIdentificator();
        if ((epic != null) && (!epics.containsKey(newId))) {
            epic.setId(newId);
            epics.put(epic.getId(), epic);
        }
    }

    public void updateEpic(int id, Epic epic) {
        if ((epic != null) && (epics.containsKey(id))) {
            epic.setId(id);
            updateEpicStatus(epic);
            epics.put(epic.getId(), epic);
        }
    }

    public void deleteEpic(int id) {
        Epic epic = getEpicByID(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks().values()) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(id);
        }
    }

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

    public HashMap<Integer, Subtask> getSubtasksOfEpic(int epicID) {
        Epic epic = getEpicByID(epicID);
        if (epic == null) {
            return null;
        }
        return epic.getSubtasks();
    }
    /// ---------------------------------

    /// Подзадачи
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.deleteSubtasks();
            updateEpicStatus(epic);
        }
        subtasks = new HashMap<>();
    }

    public Subtask getSubtaskByID(int subtaskID) {
        return subtasks.getOrDefault(subtaskID, null);
    }

    public void addSubtask(Subtask subtask) {
        int newId = getNewIdentificator();
        if ((subtask != null) && (!subtasks.containsKey(newId)) && (subtask.getEpic() != null)) {
            subtask.setId(newId);
            Epic epic = subtask.getEpic();
            epic.addSubtask(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpic());
            subtasks.put(subtask.getId(), subtask);
        }
    }

    public void updateSubtask(int id, Subtask subtask) {
        if ((subtask != null) && (subtasks.containsKey(id))) {
            subtask.setId(id);
            subtask.getEpic().getSubtasks().put(id, subtask);
            updateEpicStatus(subtask.getEpic());
            subtasks.put(subtask.getId(), subtask);
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = getSubtaskByID(id);
        if (subtask != null) {
            Epic epic = subtask.getEpic();
            epic.deleteSubtask(id);
            subtasks.remove(id);
        }
    }
    /// ---------------------------------
}
