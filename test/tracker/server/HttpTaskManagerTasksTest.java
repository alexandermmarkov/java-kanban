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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    // создаём клиент и URL
    HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:8080/tasks");
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer;
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasks();

        assertFalse(tasksFromManager.isEmpty(), "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddTaskIntersection() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task intersectingTask = new Task("IntersectingTask test 2", "Updated testing task 2",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String intersectingTaskJson = gson.toJson(intersectingTask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(intersectingTaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals("Test 2", manager.getTasks().getLast().getName(),
                "Пересечение при добавлении задачи обрабатывается некорректно");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Test 3", "Testing task 3",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedTask = new Task("Updated test 3", "Updated testing task 3",
                manager.getTasks().getFirst().getId(),
                task.getStatus().name(), LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String updatedTaskJson = gson.toJson(updatedTask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertFalse(tasksFromManager.isEmpty(), "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Updated test 3", tasksFromManager.getFirst().getName(),
                "Некорректное имя обновлённой задачи");
    }

    @Test
    public void testUpdateTaskIntersection() throws IOException, InterruptedException {
        Task taskToIntersect = new Task("Task to intersect", "Task to intersect",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskToIntersectJson = gson.toJson(taskToIntersect);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskToIntersectJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task task = new Task("Test 4", "Testing task 4",
                LocalDateTime.now().plusHours(1).format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedTask = new Task("Updated test 4", "Updated testing task 4",
                manager.getTasks().getLast().getId(),
                manager.getTasks().getLast().getStatus().name(), LocalDateTime.now().format(Task.DATE_FORMATTER),
                5);
        String updatedTaskJson = gson.toJson(updatedTask);
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals("Test 4", manager.getTasks().getLast().getName(),
                "Пересечение при обновлении задачи обрабатывается некорректно");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 5", "Testing task 5",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
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

        assertTrue(jsonElement.isJsonArray(), "Некорректное тело ответа на запрос GET /tasks");
        assertEquals(jsonElement.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(),
                task.getName(), "Запрос GET /tasks возвращает некорректный ответ");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 6", "Testing task 6",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int taskID = manager.getTasks().getLast().getId();
        URI getURL = URI.create(url + "/" + taskID);
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());

        assertTrue(jsonElement.isJsonObject(), "Некорректное тело ответа на запрос GET /tasks/" + taskID);
        assertEquals(jsonElement.getAsJsonObject().get("name").getAsString(),
                task.getName(), "Запрос GET /tasks возвращает некорректный ответ");
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        Task task = new Task("Test 7", "Testing task 7",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        int taskID = manager.getTasks().getLast().getId();
        URI getURL = URI.create(url + "/" + (taskID + 1));
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertEquals(jsonElement.getAsString(), "Задача с ID='" + (taskID + 1)
                + "' не найдена.", "Некорректное тело ответа на запрос GET /tasks/"
                + (taskID + 1));
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test 8", "Testing task 8",
                LocalDateTime.now().format(Task.DATE_FORMATTER), 5);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue((manager.getTasks().size() == 1)
                        && (manager.getTasks().getLast().getName().equals(task.getName())),
                "Задача не была добавлена в результате выполнения запроса POST /tasks/");

        int taskID = manager.getTasks().getLast().getId();
        URI getURL = URI.create(url + "/" + taskID);
        request = HttpRequest.newBuilder()
                .uri(getURL)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty(),
                "Задача не была удалена в результате выполнения запроса DELETE /tasks/" + taskID);
    }
}