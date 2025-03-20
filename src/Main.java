import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.controllers.InMemoryTaskManager;
import tracker.model.Task;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();

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

        taskManager.getTaskByID(task1.getId());
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
        System.out.println(getHistory(taskManager));
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
        String history = "";
        for (Task task : manager.getHistory()) {
            history = history + (history.isEmpty() ? "" : ", ") + task.getClass().getSimpleName() + "ID = " + task.getId();
        }
        return history;
    }
}
