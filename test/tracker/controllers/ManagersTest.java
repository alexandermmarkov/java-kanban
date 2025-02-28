package tracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ManagersTest {

    @Test
    void shouldReturnInitializedInstances() {
        assertNotNull(Managers.getDefault(), "Класс Managers не возвращает проинициализированный объект типа TaskManager.");
        assertNotNull(Managers.getDefaultHistory(), "Класс Managers не возвращает проинициализированный объект типа HistoryManager.");
    }
}