package tracker.server.handlers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.ManagerSaveException;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.TasksIntersectionException;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    void handleGetTasks(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().split("/").length == 4) {
            handleGetEpicsSubtasks(exchange);
            return;
        }
        List<Epic> epicsList = taskManager.getEpics();
        if (epicsList.isEmpty()) {
            sendNotFound(exchange, "Список эпиков пуст.");
            return;
        }
        sendText(exchange, GSON.toJson(epicsList));
    }

    void handleGetEpicsSubtasks(HttpExchange exchange) throws IOException {
        try {
            int epicID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            Map<Integer, Subtask> subtasksMap = taskManager.getEpicSubtasks(epicID);
            if (subtasksMap.isEmpty()) {
                sendNotFound(exchange, "У эпика с ID='" + epicID + "' нет подзадач.");
                return;
            }
            sendText(exchange, GSON.toJson(subtasksMap.values()));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID эпика.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    @Override
    void handleGetTask(HttpExchange exchange) throws IOException {
        try {
            int epicID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            Epic epic = taskManager.getEpicByID(epicID);
            sendText(exchange, GSON.toJson(epic));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID эпика.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    @Override
    void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            String stringEpic = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!JsonParser.parseString(stringEpic).isJsonObject()) {
                sendClientError(exchange, "Неверный формат запроса.");
                return;
            }

            Epic epic = GSON.fromJson(stringEpic, Epic.class);
            if (epic.getId() > 0) {
                taskManager.updateEpic(epic.getId(), epic);
            } else {
                taskManager.addEpic(epic);
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

    void handleDeleteTask(HttpExchange exchange) throws IOException {
        try {
            int epicID = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
            taskManager.deleteEpic(epicID);
            sendText(exchange, GSON.toJson("Эпик с ID='" + epicID + "' успешно удалён."));
        } catch (NumberFormatException e) {
            sendClientError(exchange, "Некорректный ID эпика.");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}