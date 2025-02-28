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

        /*Task task1 = new Task("Задача1", "Тестовая задача #1");
        Task task2 = new Task("Задача2", "Тестовая задача #2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);*/

        Task task1 = new Task("Задача1", "Тестовая задача #1");
        Task task2 = new Task("Задача2", "Тестовая задача #2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Эпик1", "Тестовый Эпик #1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача1", "Тестовая подзадача #1", epic1);
        Subtask subtask2 = new Subtask("Подзадача2", "Тестовая подзадача #2", epic1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

       /* Epic epic2 = new Epic("Эпик2", "Тестовый Эпик #2");
        taskManager.addEpic(epic2);
        Subtask subtask3 = new Subtask("Подзадача3", "Тестовая подзадача #3", epic2);
        taskManager.addSubtask(subtask3);
        System.out.println(taskManager.getTasks());
        System.out.println(inMemoryTaskManager.getEpics());
        System.out.println(inMemoryTaskManager.getSubtasks());*/

        /*taskManager.updateTask(1, new Task("Задача1", "Принятая в работу тестовая задача #1", "IN_PROGRESS"));
        taskManager.updateSubtask(7, new Subtask("Подзадача3", "Выполненная тестовая подзадача #3", epic2, "DONE"));
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getEpicByID(6).getStatus());
        taskManager.deleteSubtask(7);
        System.out.println(taskManager.getEpicByID(6));
        taskManager.deleteTask(1);
        System.out.println(taskManager.getTasks());
        taskManager.deleteEpic(3);
        System.out.println(taskManager.getEpics());
        inMemoryTaskManager.clearEpics();
        System.out.println(inMemoryTaskManager.getEpics());
        System.out.println(inMemoryTaskManager.getSubtasks());*/
        taskManager.getTaskByID(1);
        taskManager.getTaskByID(2);
        taskManager.getTaskByID(2);
        taskManager.getEpicByID(3);
        taskManager.getSubtaskByID(4);
        taskManager.getSubtaskByID(5);
        taskManager.getTaskByID(1);
        taskManager.getEpicByID(3);
        taskManager.getSubtaskByID(5);
        taskManager.getSubtaskByID(4);
        printAllTasks(taskManager);

        Task task = new Task("Task", "Task Description");
        taskManager.addTask(task);
        System.out.println(task.getId());
        taskManager.updateTask(task.getId(), new Task("UpdatedTask1","Updated Task1 Description","IN_PROGRESS"));
        task = taskManager.getTaskByID(task.getId());
        System.out.println(task);
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
}
