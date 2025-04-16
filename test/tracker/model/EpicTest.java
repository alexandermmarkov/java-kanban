package tracker.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

import java.util.List;
import java.util.Optional;

class EpicTest {
    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewEpic() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description");
        taskManager.addEpic(epic);
        final int epicId = epic.getId();

        final Optional<Epic> savedEpic = taskManager.getEpicByID(epicId);

        assertNotNull(savedEpic.get(), "Эпик не найден.");
        assertEquals(epic, savedEpic.get(), "Эпики не совпадают.");

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
        epic = taskManager.getEpicByID(epic.getId()).get();
        assertEquals("UpdatedEpic Updated Epic Description NEW",
                epic.getName() + " " + epic.getDescription() + " " + epic.getStatus(),
                "Эпики обновляются некорректно в Менеджере задач.");
    }

    @Test
    void deleteEpic() {
        Epic epic = new Epic("EpicToDelete", "Epic To Delete Description");
        taskManager.addEpic(epic);
        taskManager.deleteEpic(epic.getId());
        assertTrue(taskManager.getEpicByID(epic.getId()).isEmpty(), "Удаление эпиков работает некорректно в Менеджере задач.");
    }

    @Test
    void createAllSubtasksNEW() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 Description", epic);
        Subtask subtask2 = new Subtask("Subtask2", "Subtask1 Description", epic);
        Subtask subtask3 = new Subtask("Subtask3", "Subtask1 Description", epic);
        Subtask subtask4 = new Subtask("Subtask4", "Subtask1 Description", epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addSubtask(subtask4);

        assertEquals("NEW", epic.getStatus().name(),
                "Некорректный статус у Эпика, у которого всего поздадачи NEW.");
    }

    @Test
    void createAllSubtasksDONE() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 Description", epic);
        subtask1.setStatus("DONE");
        Subtask subtask2 = new Subtask("Subtask2", "Subtask1 Description", epic);
        subtask2.setStatus("DONE");
        Subtask subtask3 = new Subtask("Subtask3", "Subtask1 Description", epic);
        subtask3.setStatus("DONE");
        Subtask subtask4 = new Subtask("Subtask4", "Subtask1 Description", epic);
        subtask4.setStatus("DONE");
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addSubtask(subtask4);

        assertEquals("DONE", epic.getStatus().name(),
                "Некорректный статус у Эпика, у которого всего поздадачи DONE.");
    }

    @Test
    void createSubtasksNEWAndDONE() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 Description", epic);
        subtask1.setStatus("NEW");
        Subtask subtask2 = new Subtask("Subtask2", "Subtask1 Description", epic);
        subtask2.setStatus("DONE");
        Subtask subtask3 = new Subtask("Subtask3", "Subtask1 Description", epic);
        subtask3.setStatus("NEW");
        Subtask subtask4 = new Subtask("Subtask4", "Subtask1 Description", epic);
        subtask4.setStatus("DONE");
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addSubtask(subtask4);

        assertEquals("IN_PROGRESS", epic.getStatus().name(),
                "Некорректный статус у Эпика, у которого поздадачи NEW и DONE.");
    }

    @Test
    void createAllSubtasksINPROGRESS() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 Description", epic);
        subtask1.setStatus("IN_PROGRESS");
        Subtask subtask2 = new Subtask("Subtask2", "Subtask1 Description", epic);
        subtask2.setStatus("IN_PROGRESS");
        Subtask subtask3 = new Subtask("Subtask3", "Subtask1 Description", epic);
        subtask3.setStatus("IN_PROGRESS");
        Subtask subtask4 = new Subtask("Subtask4", "Subtask1 Description", epic);
        subtask4.setStatus("IN_PROGRESS");
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addSubtask(subtask4);

        assertEquals("IN_PROGRESS", epic.getStatus().name(),
                "Некорректный статус у Эпика, у которого все поздадачи IN_PROGRESS.");
    }

    @Test
    void shouldChangeStatusAfterSubtasksUpdate() {
        Epic epic = new Epic("Epic", "Epic Description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Subtask1 Description", epic);
        subtask1.setStatus("NEW");
        Subtask subtask2 = new Subtask("Subtask2", "Subtask1 Description", epic);
        subtask2.setStatus("NEW");
        Subtask subtask3 = new Subtask("Subtask3", "Subtask1 Description", epic);
        subtask3.setStatus("NEW");
        Subtask subtask4 = new Subtask("Subtask4", "Subtask1 Description", epic);
        subtask4.setStatus("NEW");
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addSubtask(subtask4);
        taskManager.updateSubtask(subtask1.getId(), new Subtask(subtask1.getName(), subtask1.getDescription(), epic,
                "IN_PROGRESS"));

        assertEquals("IN_PROGRESS", epic.getStatus().name(),
                "Некорректный статус у Эпика, у которого она одна или несколько задач обновили статус.");
    }
}