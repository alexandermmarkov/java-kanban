package tracker.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //System.out.println("Началась обработка /history запроса от клиента.");

        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equals("GET") && (pathParts.length == 2)) {
            handleGetHistory(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendText(exchange, GSON.toJson(history));
    }
}