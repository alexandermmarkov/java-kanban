package tracker.server;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.exceptions.ManagerSaveException;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.TasksIntersectionException;
import tracker.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //System.out.println("Началась обработка /tasks запроса от клиента.");

        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASK: {
                handleGetTask(exchange);
                break;
            }
            case POST_TASK: {
                handlePostTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(exchange);
                break;
            }
            default:
                sendNotFound(exchange, "Такого эндпоинта не существует.");
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> taskList = taskManager.getTasks();
        if (taskList.isEmpty()) {
            sendNotFound(exchange, "Список задач пуст.");
            return;
        }
        sendText(exchange, GSON.toJson(taskList));
    }

    private void handleGetTask(HttpExchange exchange) throws IOException {
        try {
            int taskID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            Task task = taskManager.getTaskByID(taskID);
            sendText(exchange, GSON.toJson(task));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID задачи.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            String stringTask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!JsonParser.parseString(stringTask).isJsonObject()) {
                sendClientError(exchange, "Неверный формат запроса.");
                return;
            }

            Task task = GSON.fromJson(stringTask, Task.class);
            if (task.getStatus() == null) {
                task.setStatus("NEW");
            }
            if (task.getId() > 0) {
                taskManager.updateTask(task.getId(), task);
            } else {
                taskManager.addTask(task);
            }
            sendCreated(exchange);
        } catch (JsonSyntaxException e) {
            sendClientError(exchange, "Некорректный JSON:\n" + e.getMessage());
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TasksIntersectionException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (ManagerSaveException | IOException e) {
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        try {
            int taskID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            taskManager.deleteTask(taskID);
            sendText(exchange, GSON.toJson("Задача с ID='" + taskID + "' успешно удалена."));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID задачи.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}