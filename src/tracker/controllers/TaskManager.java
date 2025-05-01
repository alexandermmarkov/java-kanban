package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.LocalDateTime;
import java.util.*;

public interface TaskManager {
    ArrayList<Task> getTasks();

    void clearTasks();

    Task getTaskByID(int taskID);

    void addTask(Task task);

    void updateTask(int id, Task task);

    void deleteTask(int id);

    ArrayList<Epic> getEpics();

    void clearEpics();

    Epic getEpicByID(int epicID);

    void addEpic(Epic epic);

    void updateEpic(int id, Epic epic);

    void deleteEpic(int id);

    void updateEpicStatus(Epic epic);

    void updateEpicTime(Epic epic);

    HashMap<Integer, Subtask> getEpicSubtasks(int epicID);

    ArrayList<Subtask> getSubtasks();

    void clearSubtasks();

    Subtask getSubtaskByID(int subtaskID);

    void addSubtask(Subtask subtask);

    void updateSubtask(int id, Subtask subtask);

    void deleteSubtask(int id);

    List<Task> getHistory();

    Map<Integer, Task> getTasksMap();

    Map<Integer, Epic> getEpicsMap();

    Map<Integer, Subtask> getSubtasksMap();

    List<Task> getPrioritizedTasks();

    boolean isNotIntersect(Task taskToCheck);

    void setTaskIntervals(Integer taskID, LocalDateTime startTime, LocalDateTime endTime);

    Map<LocalDateTime, Integer> getTaskIntervals();

    void releaseTaskIntervals(Task task);
}
