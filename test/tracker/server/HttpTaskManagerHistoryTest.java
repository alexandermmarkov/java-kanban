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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerHistoryTest {
    // создаём клиент и URL
    HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:8080/history");
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer;
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerHistoryTest() throws IOException {
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
    public void testGetHistory() throws IOException, InterruptedException {
        /// создаём задачу
        Task task = new Task("Test task", "Testing task",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);
        URI postTaskURL = URI.create(url.toString().replace("history", "tasks"));
        // создаём запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(postTaskURL)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        /// создаём эпик
        Epic epic = new Epic("Test epic", "Testing epic");
        // конвертируем его в JSON
        String epicJson = gson.toJson(epic);
        URI postEpicURL = URI.create(url.toString().replace("history", "epics"));
        // создаём новый запрос
        request = HttpRequest.newBuilder()
                .uri(postEpicURL)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        // вызываем рест, отвечающий за создание эпиков
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        /// создаём подзадачу
        Subtask subtask = new Subtask("Test subtask", "Testing subtask",
                manager.getEpics().getLast().getId());
        // конвертируем её в JSON
        String subtaskJson = gson.toJson(subtask);
        URI postSubtaskURL = URI.create(url.toString().replace("history", "subtasks"));
        request = HttpRequest.newBuilder()
                .uri(postSubtaskURL)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        // вызываем рест, отвечающий за создание подзадач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        int taskID = manager.getTasks().getLast().getId();
        int epicID = manager.getEpics().getLast().getId();
        int subtaskID = manager.getSubtasks().getLast().getId();

        /// заполняем историю
        assertEquals(200, getTaskById("epics", epicID));
        assertEquals(200, getTaskById("tasks", taskID));
        assertEquals(200, getTaskById("subtasks", subtaskID));
        assertEquals(200, getTaskById("epics", epicID));
        assertEquals(200, getTaskById("subtasks", subtaskID));

        /// получаем историю
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray(), "Некорректное тело ответа на запрос GET /history");
        assertTrue(jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt()
                        == manager.getHistory().getFirst().getId()
                        && jsonElement.getAsJsonArray().get(2).getAsJsonObject().get("id").getAsInt()
                        == manager.getHistory().getLast().getId()
                        && jsonElement.getAsJsonArray().size() == manager.getHistory().size()
                        && manager.getHistory().size() == 3,
                "Запрос GET /history возвращает некорректный ответ");
    }

    public int getTaskById(String path, int taskID) throws IOException, InterruptedException {
        URI getURL = URI.create(url.toString().replace("history", path) + "/" + taskID);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }
}