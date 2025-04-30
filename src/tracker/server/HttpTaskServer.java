package tracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private final HttpServer httpServer;

    public HttpTaskServer() throws IOException {
        taskManager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    public static void main(String[] args) throws IOException {
        //{"name":"задача","description":"описание задачи","startTime":"29.04.2025 12:22","duration":60}
        //{"name":"эпик","description":"описание эпика"}
        //{"name":"подзадача","description":"описание подзадачи","startTime":"30.04.2025 12:22","duration":60,"epicId":2}
        new HttpTaskServer().start();
    }

    public static Gson getGson() {
        return BaseHttpHandler.GSON;
    }

    public void start() throws IOException {
        // настройка и запуск HTTP-сервера
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
        httpServer.start(); // запускаем сервер

        //System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        httpServer.stop(0); // останавливаем сервер

        //System.out.println("HTTP-сервер остановлен на " + PORT + " порту!");
    }
}