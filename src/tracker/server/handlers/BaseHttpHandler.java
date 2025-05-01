package tracker.server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.server.adapters.DurationAdapter;
import tracker.server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
            .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
            .create();
    public static final String CONTENTTYPE_JSON = "application/json;charset=utf-8";
    protected TaskManager taskManager;

    protected BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    private enum Endpoint {
        GET_TASKS,
        GET_TASK,
        POST_TASK,
        DELETE_TASK,
        UNKNOWN
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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
                sendMethodNotAllowed(exchange);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        switch (requestMethod) {
            case "GET":
                if ((pathParts.length == 2) || (pathParts.length == 4 && pathParts[1].equals("epics"))) {
                    return Endpoint.GET_TASKS;
                } else if (pathParts.length == 3) {
                    return Endpoint.GET_TASK;
                }
                break;
            case "POST":
                if (pathParts.length == 2) {
                    return Endpoint.POST_TASK;
                }
                break;
            case "DELETE":
                if (pathParts.length == 3) {
                    return Endpoint.DELETE_TASK;
                }
                break;
            default:
                return Endpoint.UNKNOWN;
        }

        return Endpoint.UNKNOWN;
    }

    abstract void handleGetTasks(HttpExchange exchange) throws IOException;

    abstract void handleGetTask(HttpExchange exchange) throws IOException;

    abstract void handlePostTask(HttpExchange exchange) throws IOException;

    abstract void handleDeleteTask(HttpExchange exchange) throws IOException;

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", CONTENTTYPE_JSON);
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreated(HttpExchange h) throws IOException {
        h.sendResponseHeaders(201, 0);
        h.close();
    }

    protected void sendClientError(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", CONTENTTYPE_JSON);
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", CONTENTTYPE_JSON);
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendMethodNotAllowed(HttpExchange h) throws IOException {
        byte[] resp = GSON.toJson("Такого эндпоинта не существует.").getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", CONTENTTYPE_JSON);
        h.sendResponseHeaders(405, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", CONTENTTYPE_JSON);
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendServerError(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", CONTENTTYPE_JSON);
        h.sendResponseHeaders(500, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}