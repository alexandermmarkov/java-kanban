package tracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.Optional;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void initializeTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldAddAndFindAllTypesOfTasks() {
        Task task = createTask(1);
        Epic epic = createEpic(1);
        Subtask subtask = createSubtask(epic, 1);

        taskManager.addTask(task);
        final int taskId = task.getId();
        final Optional<Task> savedTask = taskManager.getTaskByID(taskId);

        taskManager.addEpic(epic);
        final int epicId = epic.getId();
        final Optional<Epic> savedEpic = taskManager.getEpicByID(epicId);

        taskManager.addSubtask(subtask);
        final int subtaskId = subtask.getId();
        final Optional<Subtask> savedSubtask = taskManager.getSubtaskByID(subtaskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask.get(), "Задачи не совпадают.");

        assertNotNull(savedEpic, "Эпик не найдена.");
        assertEquals(epic, savedEpic.get(), "Эпики не совпадают.");

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask.get(), "Подзадачи не совпадают.");
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
        assertEquals(task1, taskManager.getTaskByID(task1.getId()).get(), "Задача с ID = '" + task1.getId()
                + "' изменена после добавления в Менеджер задач.");
        assertEquals(task2, taskManager.getTaskByID(task2.getId()).get(), "Задача с ID = '" + task2.getId()
                + "' изменена после добавления в Менеджер задач.");
    }

    @Test
    void shouldDeleteSubtaskFromEpic() {
        Epic epic = createEpic(1);
        Subtask subTask1 = createSubtask(epic, 1);
        Subtask subTask2 = createSubtask(epic, 2);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subTask1);
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