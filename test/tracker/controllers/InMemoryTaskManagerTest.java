package tracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void initializeTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldAddAndFindAllTypesOfTasks() {
        Task task = createTask(1);
        taskManager.addTask(task);
        final int taskId = task.getId();

        Epic epic = createEpic(1);
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        Subtask subtask = createSubtask(epic, 1);
        taskManager.addSubtask(subtask);
        final int subtaskId = subtask.getId();

        assertDoesNotThrow(() -> taskManager.getTaskByID(taskId), "Задача не найдена.");
        assertEquals(task, taskManager.getTaskByID(taskId), "Задачи не совпадают.");

        assertDoesNotThrow(() -> taskManager.getEpicByID(epicId), "Эпик не найден.");
        assertEquals(epic, taskManager.getEpicByID(epicId), "Эпики не совпадают.");

        assertDoesNotThrow(() -> taskManager.getSubtaskByID(subtaskId), "Подзадача не найдена.");
        assertEquals(subtask, taskManager.getSubtaskByID(subtaskId), "Подзадачи не совпадают.");
    }

    @Test
    void shouldAssignNewIdIfAlreadyUsed() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        Task task3 = createTask(3);
        Task task4 = createTask(4);

        taskManager.addTask(task1);
        task2.setId(1);
        taskManager.addTask(task2);
        assertNotEquals(task1.getId(), task2.getId(), "Конфликт - у обеих задач ID = '" + task1.getId() + "'.");

        task3.setId(1);
        taskManager.addTask(task3);
        taskManager.addTask(task4);
        assertNotEquals(task3.getId(), task4.getId(), "Конфликт - у обеих задач ID = '" + task3.getId() + "'.");
    }

    @Test
    void shouldKeepTasksInManagerUnaltered() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        assertEquals(task1, taskManager.getTaskByID(task1.getId()), "Задача с ID = '" + task1.getId()
                + "' изменена после добавления в Менеджер задач.");
        assertEquals(task2, taskManager.getTaskByID(task2.getId()), "Задача с ID = '" + task2.getId()
                + "' изменена после добавления в Менеджер задач.");
    }

    @Test
    void shouldDeleteSubtaskFromEpic() {
        Epic epic = createEpic(1);
        taskManager.addEpic(epic);
        Subtask subTask1 = createSubtask(epic, 1);
        taskManager.addSubtask(subTask1);
        Subtask subTask2 = createSubtask(epic, 2);
        taskManager.addSubtask(subTask2);
        taskManager.deleteSubtask(subTask1.getId());
        assertTrue((epic.getSubtasks().size() == 1) && (!epic.getSubtasks().containsKey(subTask1.getId())),
                "ID удаляемой задачи должен удаляться из таблицы подзадач");
    }

    @Test
    void shouldBeEqualWithTheSameID() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        task2.setId(task1.getId());
        assertEquals(task1, task2, "Задачи с одинаковыми ID некорректно считаются разными");
    }
}