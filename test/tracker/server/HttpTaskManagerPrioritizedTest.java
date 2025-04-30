package tracker.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerPrioritizedTest {
    // создаём клиент и URL
    HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:8080/prioritized");
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer;
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerPrioritizedTest() throws IOException {
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
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        /// создаём 1-ую задачу
        Task task1 = new Task("Test task 1", "Testing task1",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task1);
        URI postTask1URL = URI.create(url.toString().replace("prioritized", "tasks"));
        // создаём запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(postTask1URL)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        /// создаём 2-ую задачу
        Task task2 = new Task("Test task 2", "Testing task2",
                LocalDateTime.now().plusHours(2).format(Task.DATE_FORMATTER), 30);
        // конвертируем её в JSON
        String task2Json = gson.toJson(task2);
        URI postTask2URL = URI.create(url.toString().replace("prioritized", "tasks"));
        // создаём новый запрос
        request = HttpRequest.newBuilder()
                .uri(postTask2URL)
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();
        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        /// создаём 3-ю задачу
        Task task3 = new Task("Test task 3", "Testing task3",
                LocalDateTime.now().minusMinutes(30).format(Task.DATE_FORMATTER), 15);
        // конвертируем её в JSON
        String task3Json = gson.toJson(task3);
        URI postTask3URL = URI.create(url.toString().replace("prioritized", "tasks"));
        // создаём новый запрос
        request = HttpRequest.newBuilder()
                .uri(postTask3URL)
                .POST(HttpRequest.BodyPublishers.ofString(task3Json))
                .build();
        // вызываем рест, отвечающий за создание задач
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        /// получаем prioritized список
        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonArray(), "Некорректное тело ответа на запрос GET /prioritized");
        assertTrue(jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt()
                        == manager.getPrioritizedTasks().getFirst().getId()
                        && jsonElement.getAsJsonArray().get(2).getAsJsonObject().get("id").getAsInt()
                        == manager.getPrioritizedTasks().getLast().getId()
                        && jsonElement.getAsJsonArray().size() == manager.getPrioritizedTasks().size()
                        && manager.getPrioritizedTasks().size() == 3,
                "Запрос GET /prioritized возвращает некорректный ответ");
    }
}