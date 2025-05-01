package tracker.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest {
    // создаём клиент и URL
    HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:8080/subtasks");
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer;
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerSubtasksTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.clearTasks();
        manager.clearSubtasks();
        manager.clearEpics();
        Task.resetIdentificator();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtasks() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic", "Test 1 epic");
        // конвертируем его в JSON
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));
        // создаём запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        // вызываем рест, отвечающий за создание эпиков
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // создаём подзадачу
        Subtask subtask = new Subtask("Test 1", "Testing subtask 1",
                manager.getEpics().getLast().getId());
        // конвертируем её в JSON
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        // вызываем рест, отвечающий за создание подзадач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна подзадача с корректным именем
        List<Subtask> subtasksFromManager = manager.getSubtasks();

        assertFalse(subtasksFromManager.isEmpty(), "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 1", subtasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
        assertEquals(manager.getEpics().getLast().getSubtasks().values().stream().toList().getLast().getName(),
                subtasksFromManager.getLast().getName(), "Привязка подзадачи к эпику выполнена некорректно.");
    }

    @Test
    public void testAddSubtasksIntersection() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 2 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 2", "Testing subtask 2",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask intersectingSubtask = new Subtask("Intersecting subtask", "Test 2 subtask",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String intersectingSubtaskJson = gson.toJson(intersectingSubtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(intersectingSubtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals("Test 2", manager.getSubtasks().getLast().getName(),
                "Пересечение при добавлении подзадачи обрабатывается некорректно");
    }

    @Test
    public void testUpdateSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 3 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 3", "Testing subtask 3",
                manager.getEpics().getLast().getId());
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask updatedSubtask = new Subtask("Updated test 3", "Updated testing subtask 3",
                manager.getEpics().getLast().getId(), manager.getSubtasks().getLast().getId());
        String updatedSubtaskJson = gson.toJson(updatedSubtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();

        assertFalse(subtasksFromManager.isEmpty(), "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Updated test 3", subtasksFromManager.getFirst().getName(),
                "Некорректное имя обновлённой подзадачи");
    }

    @Test
    public void testUpdateSubtasksIntersection() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 4 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task subtaskToIntersect = new Subtask("Subtask to intersect", "Subtask to intersect",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String taskToIntersectJson = gson.toJson(subtaskToIntersect);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskToIntersectJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 4", "Testing subtask 4",
                manager.getEpics().getLast().getId(), LocalDateTime.now().plusHours(1).format(Task.DATE_FORMATTER),
                5);
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask updatedSubtask = new Subtask("Updated test 4", "Updated testing subtask 4",
                manager.getEpics().getLast().getId(), manager.getSubtasks().getLast().getId(),
                manager.getSubtasks().getLast().getStatus().name(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String updatedSubtaskJson = gson.toJson(updatedSubtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals("Test 4", manager.getSubtasks().getLast().getName(),
                "Пересечение при обновлении подзадачи обрабатывается некорректно");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 5 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 5", "Testing subtask 5",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray(), "Некорректное тело ответа на запрос GET /subtasks");
        assertEquals(jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(),
                subtask.getName(), "Запрос GET /subtasks возвращает некорректный ответ");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 6 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 6", "Testing subtask 6",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int subtaskID = manager.getSubtasks().getLast().getId();
        URI getURL = URI.create(url + "/" + subtaskID);
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonObject(), "Некорректное тело ответа на запрос GET /subtasks/" + subtaskID);
        assertEquals(jsonElement.getAsJsonObject().get("name").getAsString(),
                subtask.getName(), "Запрос GET /subtasks/" + subtaskID + " возвращает некорректный ответ");
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 7 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 7", "Testing subtask 7",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int subtaskID = manager.getSubtasks().getLast().getId();
        URI getURL = URI.create(url + "/" + (subtaskID + 1));
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertEquals(jsonElement.getAsString(), "Подзадача с ID='" + (subtaskID + 1)
                + "' не найдена.", "Некорректное тело ответа на запрос GET /subtasks/" + (subtaskID + 1));
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Test 8 epic");
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("subtasks", "epics"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtask = new Subtask("Test 8", "Testing subtask 8",
                manager.getEpics().getLast().getId(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String subtaskJson = gson.toJson(subtask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        assertTrue((manager.getSubtasks().size() == 1)
                        && (manager.getSubtasks().getLast().getName().equals(subtask.getName())),
                "Подзадача не была добавлена в результате выполнения запроса POST /subtasks/");

        int subtaskID = manager.getSubtasks().getLast().getId();
        URI getURL = URI.create(url + "/" + subtaskID);
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty(),
                "Подзадача не была удалена в результате выполнения запроса DELETE /subtasks/" + subtaskID);
    }
}