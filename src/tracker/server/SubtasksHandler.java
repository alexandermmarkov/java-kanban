package tracker.server;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.exceptions.ManagerSaveException;
import tracker.exceptions.NotFoundException;
import tracker.exceptions.TasksIntersectionException;
import tracker.model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //System.out.println("Началась обработка /subtasks запроса от клиента.");

        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetSubtasks(exchange);
                break;
            }
            case GET_TASK: {
                handleGetSubtask(exchange);
                break;
            }
            case POST_TASK: {
                handlePostSubtask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteSubtask(exchange);
                break;
            }
            default:
                sendNotFound(exchange, "Такого эндпоинта не существует.");
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtaskList = taskManager.getSubtasks();
        if (subtaskList.isEmpty()) {
            sendNotFound(exchange, "Список подзадач пуст.");
            return;
        }
        sendText(exchange, GSON.toJson(subtaskList));
    }

    private void handleGetSubtask(HttpExchange exchange) throws IOException {
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

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
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

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
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