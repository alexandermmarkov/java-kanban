package tracker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseHttpHandler {
    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
            .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
            .create();
    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
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
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendServerError(HttpExchange h, String text) throws IOException {
        byte[] resp = GSON.toJson(text).getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(500, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    enum Endpoint {
        GET_TASKS,
        GET_TASK,
        POST_TASK,
        DELETE_TASK,
        UNKNOWN
    }

    protected Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        switch (requestMethod) {
            case "GET":
                if (pathParts.length == 2) {
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

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter dtf = Task.DATE_FORMATTER;

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
            jsonWriter.value(localDateTime.format(dtf));
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), dtf);
        }
    }

    static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
            jsonWriter.value(duration.toMinutes());
        }

        @Override
        public Duration read(final JsonReader jsonReader) throws IOException {
            return Duration.ofMinutes(jsonReader.nextInt());
        }
    }
}