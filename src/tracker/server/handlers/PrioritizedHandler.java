package tracker.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        sendText(exchange, GSON.toJson(prioritizedTasks));
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