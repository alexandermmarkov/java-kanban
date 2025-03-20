package tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

import java.util.List;

class EpicTest {

    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewEpic() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        final Epic savedEpic = taskManager.getEpicByID(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void shouldNotBeAbleToAddEpicAsASubtask() {
        Epic epic = new Epic("Epic1", "Epic1");
        epic.addSubtask(epic);
        assertNull(epic.getSubtasks().get(1), "Нельзя добавлять эпики в качестве подзадачи.");
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);
        taskManager.updateEpic(epic.getId(), new Epic("UpdatedEpic", "Updated Epic Description"));
        epic = taskManager.getEpicByID(epic.getId());
        assertEquals("UpdatedEpic Updated Epic Description NEW",
                epic.getName() + " " + epic.getDescription() + " " + epic.getStatus(),
                "Эпики обновляются некорректно в Менеджере задач.");
    }

    @Test
    void deleteEpic() {
        Epic epic = new Epic("EpicToDelete", "Epic To Delete Description");
        taskManager.addEpic(epic);
        taskManager.deleteEpic(epic.getId());
        epic = taskManager.getEpicByID(epic.getId());
        assertNull(epic, "Удаление эпиков работает некорректно в Менеджере задач.");
    }
}