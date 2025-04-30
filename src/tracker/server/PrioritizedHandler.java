package tracker.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //System.out.println("Началась обработка /prioritized запроса от клиента.");

        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equals("GET") && (pathParts.length == 2)) {
            handleGetPrioritizedTasks(exchange);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        sendText(exchange, GSON.toJson(prioritizedTasks));
    }
}