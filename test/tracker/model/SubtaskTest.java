package tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class SubtaskTest {
    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewSubtask() {
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description",
                new Epic("Epic", "Epic"), LocalDateTime.now().format(Task.DATE_FORMATTER),
                60);
        taskManager.addSubtask(subtask);
        final int subtaskId = subtask.getId();

        final Optional<Subtask> savedSubtask = taskManager.getSubtaskByID(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask.get(), "Подзадача не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Epic", "Epic");
        Subtask subtask = new Subtask("Subtask", "Subtask Description", epic,
                LocalDateTime.now().format(Task.DATE_FORMATTER), 10);
        taskManager.addSubtask(subtask);
        taskManager.updateSubtask(subtask.getId(), new Subtask("UpdatedSubtask",
                "Updated Subtask Description", epic, "IN_PROGRESS"));
        subtask = taskManager.getSubtaskByID(subtask.getId()).get();
        assertEquals("UpdatedSubtask Updated Subtask Description IN_PROGRESS",
                subtask.getName() + " " + subtask.getDescription() + " " + subtask.getStatus(),
                "Подзадачи обновляются некорректно в Менеджере задач.");
    }

    @Test
    void deleteSubtask() {
        Task task = new Task("TaskToDelete", "Task To Delete Description",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 180);
        taskManager.addTask(task);
        taskManager.deleteTask(task.getId());
        assertTrue(taskManager.getTaskByID(task.getId()).isEmpty(),
                "Удаление задач работает некорректно в Менеджере задач.");
    }

    @Test
    void epicIsLinked() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Subtask Description", epic,
                LocalDateTime.now().format(Task.DATE_FORMATTER), 10);
        taskManager.addSubtask(subtask);
        assertEquals(subtask.getEpic().getId(), epic.getId(), "Эпик не был привзян к подзадаче.");
    }
}