import tracker.controllers.FileBackedTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File file = new File("C:\\Test\\Sprint7Test.txt"); /// 0. Создание объекта файла
        loadIntoFile(new FileBackedTaskManager(file)); /// 1. Заполнение файла
        loadFromFile(file); /// 2. Проверка загруженных в менеджер задач из файла

        /*taskManager.getTaskByID(task1.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getTaskByID(task2.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getTaskByID(task1.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getEpicByID(epic2.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getSubtaskByID(subtask3.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getSubtaskByID(subtask2.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getSubtaskByID(subtask1.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getTaskByID(task1.getId());
        System.out.println(getHistory(taskManager));
        taskManager.getEpicByID(epic1.getId());
        System.out.println(getHistory(taskManager));
        taskManager.deleteEpic(epic1.getId());
        System.out.println(getHistory(taskManager));*/
        //printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getSubtasksOfEpic(epic.getId()).values()) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

    private static String getHistory(TaskManager manager) {
        StringBuilder history = new StringBuilder();
        for (Task task : manager.getHistory()) {
            history.append((history.isEmpty()) ? "" : ", ").append(task.getClass().getSimpleName()).append("ID = ").append(task.getId());
        }
        return history.toString();
    }

    private static void loadIntoFile(TaskManager manager) {
        Task task1 = new Task("Задача1", "Тестовая задача #1");
        Task task2 = new Task("Задача2", "Тестовая задача #2");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Эпик1", "Тестовый Эпик #1");
        Epic epic2 = new Epic("Эпик2", "Тестовый Эпик #2");
        manager.addEpic(epic1);
        manager.addEpic(epic2);

        Subtask subtask1 = new Subtask("Подзадача1", "Тестовая подзадача #1", epic1);
        Subtask subtask2 = new Subtask("Подзадача2", "Тестовая подзадача #2", epic1);
        Subtask subtask3 = new Subtask("Подзадача3", "Тестовая подзадача #3", epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);
    }

    private static void loadFromFile(File file) {
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(file);
        for (Task task : taskManager.getAllTasks().values()) {
            System.out.println(taskManager.toString(task));
        }
        System.out.println("История:");
        taskManager.getTaskByID(2);
        taskManager.getTaskByID(1);
        taskManager.getSubtaskByID(5);
        System.out.println(taskManager.getHistory());
    }
}
