package tracker.server.handlers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.ManagerSaveException;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.TasksIntersectionException;
import tracker.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> taskList = taskManager.getTasks();
        if (taskList.isEmpty()) {
            sendNotFound(exchange, "Список задач пуст.");
            return;
        }
        sendText(exchange, GSON.toJson(taskList));
    }

    @Override
    void handleGetTask(HttpExchange exchange) throws IOException {
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

    @Override
    void handlePostTask(HttpExchange exchange) throws IOException {
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

    @Override
    void handleDeleteTask(HttpExchange exchange) throws IOException {
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