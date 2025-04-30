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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {
    // создаём клиент и URL
    HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:8080/epics");
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer;
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Test 1", "Testing epic 1");
        // конвертируем его в JSON
        String epicJson = gson.toJson(epic);

        // создаём запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        // вызываем рест, отвечающий за создание эпиков
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создался один эпик с корректным именем
        List<Epic> epicsFromManager = manager.getEpics();

        assertFalse(epicsFromManager.isEmpty(), "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test 1", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray(), "Некорректное тело ответа на запрос GET /epics");
        assertEquals(jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(),
                epic.getName(), "Запрос GET /epics возвращает некорректный ответ");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 3", "Testing epic 3");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int epicID = manager.getEpics().getLast().getId();
        URI getURL = URI.create(url + "/" + epicID);
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonObject(), "Некорректное тело ответа на запрос GET /epics/" + epicID);
        assertEquals(jsonElement.getAsJsonObject().get("name").getAsString(),
                epic.getName(), "Запрос GET /epics возвращает некорректный ответ");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 4", "Testing epic 4");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int epicID = manager.getEpics().getLast().getId();
        URI getURL = URI.create(url + "/" + (epicID + 1));
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertEquals(jsonElement.getAsString(), "Эпик с ID='" + (epicID + 1)
                + "' не найден.", "Некорректное тело ответа на запрос GET /epics/"
                + (epicID + 1));
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 5", "Testing epic 5");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int epicID = manager.getEpics().getLast().getId();

        Subtask subtask = new Subtask("Subtask", "Test 5 subtask", epicID);
        String subtaskJson = gson.toJson(subtask);
        URI postSubtaskURL = URI.create(url.toString().replace("epics", "subtasks"));
        request = HttpRequest.newBuilder()
                .uri(postSubtaskURL)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        URI getURL = URI.create(url + "/" + epicID + "/subtasks");
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray(), "Некорректное тело ответа на запрос GET /epics/" + epicID + "/subtasks");
        assertEquals(jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(),
                subtask.getName(), "Запрос GET /epics/" + epicID + " возвращает некорректный ответ");
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 6", "Testing epic 6");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int epicID = manager.getEpics().getLast().getId();

        Subtask subtask = new Subtask("Subtask", "Test 6 subtask", epicID);
        String subtaskJson = gson.toJson(subtask);
        URI postSubtaskURL = URI.create(url.toString().replace("epics", "subtasks"));
        request = HttpRequest.newBuilder()
                .uri(postSubtaskURL)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        URI getURL = URI.create(url + "/" + (epicID + 1) + "/subtasks");
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertEquals(jsonElement.getAsString(), "Эпик с ID='" + (epicID + 1)
                + "' не найден.", "Некорректное тело ответа на запрос GET /epics/"
                + (epicID + 1) + "/subtasks");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 7", "Testing task 7");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue((manager.getEpics().size() == 1)
                        && (manager.getEpics().getLast().getName().equals(epic.getName())),
                "Эпик не был добавлен в результате выполнения запроса POST /epics/");

        int epicID = manager.getEpics().getLast().getId();
        URI getURL = URI.create(url + "/" + epicID);
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty(),
                "Эпик не был удалён в результате выполнения запроса DELETE /epics/" + epicID);
    }
}