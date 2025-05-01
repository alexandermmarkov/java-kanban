package tracker.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendText(exchange, GSON.toJson(history));
    }

    @Override
    void handleGetTask(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange);
    }

    @Override
    void handlePostTask(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange);
    }

    @Override
    void handleDeleteTask(HttpExchange exchange) throws IOException {
        sendMethodNotAllowed(exchange);
    }
}