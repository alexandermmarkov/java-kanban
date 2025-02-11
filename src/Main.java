public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();

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

        Epic epic2 = new Epic("Эпик2", "Тестовый Эпик #2");
        taskManager.addEpic(epic2);
        Subtask subtask3 = new Subtask("Подзадача3", "Тестовая подзадача #3", epic2);
        taskManager.addSubtask(subtask3);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        taskManager.updateTask(1, new Task("Задача1", "Принятая в работу тестовая задача #1", "IN_PROGRESS"));
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
    }
}
