package tracker.server.handlers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.ManagerSaveException;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.TasksIntersectionException;
import tracker.model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtaskList = taskManager.getSubtasks();
        if (subtaskList.isEmpty()) {
            sendNotFound(exchange, "Список подзадач пуст.");
            return;
        }
        sendText(exchange, GSON.toJson(subtaskList));
    }

    @Override
    void handleGetTask(HttpExchange exchange) throws IOException {
        try {
            int subtaskID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            Subtask subtask = taskManager.getSubtaskByID(subtaskID);
            sendText(exchange, GSON.toJson(subtask));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID подзадачи.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    @Override
    void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            String stringSubtask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!JsonParser.parseString(stringSubtask).isJsonObject()) {
                sendClientError(exchange, "Неверный формат запроса.");
                return;
            }

            Subtask subtask = GSON.fromJson(stringSubtask, Subtask.class);
            if (subtask.getStatus() == null) {
                subtask.setStatus("NEW");
            }
            if (subtask.getId() > 0) {
                taskManager.updateSubtask(subtask.getId(), subtask);
            } else {
                taskManager.addSubtask(subtask);
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
            int subtaskID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            taskManager.deleteSubtask(subtaskID);
            sendText(exchange, GSON.toJson("Подзадача с ID='" + subtaskID + "' успешно удалена."));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID подзадачи.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}