package tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

import java.util.List;

class SubtaskTest {

    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewSubtask() {
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", new Epic("Epic", "Epic"));
        taskManager.addSubtask(subtask);
        final int subtaskId = subtask.getId();

        final Subtask savedSubtask = taskManager.getSubtaskByID(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадача не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Epic", "Epic");
        Subtask subtask = new Subtask("Subtask", "Subtask Description", epic);
        taskManager.addSubtask(subtask);
        taskManager.updateSubtask(subtask.getId(), new Subtask("UpdatedSubtask", "Updated Subtask Description", epic, "IN_PROGRESS"));
        subtask = taskManager.getSubtaskByID(subtask.getId());
        assertEquals("UpdatedSubtask Updated Subtask Description IN_PROGRESS",
                subtask.getName() + " " + subtask.getDescription() + " " + subtask.getStatus(),
                "Подзадачи обновляются некорректно в Менеджере задач.");
    }

    @Test
    void deleteSubtask() {
        Task task = new Task("TaskToDelete", "Task To Delete Description");
        taskManager.addTask(task);
        taskManager.deleteTask(task.getId());
        task = taskManager.getTaskByID(task.getId());
        assertNull(task, "Удаление задач работает некорректно в Менеджере задач.");
    }

}