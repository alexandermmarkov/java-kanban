package tracker.history;

import tracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    public Node head;
    public Node tail;
    private final Map<Integer, Node> nodes;

    public InMemoryHistoryManager() {
        nodes = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        linkLast(task);
    }

    @Override
    public void remove(int taskID) {
        if (nodes.containsKey(taskID)) {
            Node node = nodes.get(taskID);
            nodes.remove(taskID);
            removeNode(node);
        }
    }

    public void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        node.prev = null;
        node.next = null;
    }

    public void linkLast(Task task) {
        Node node = nodes.get(task.getId());
        if (node != null) {
            nodes.remove(task.getId());
            removeNode(node);
        }

        Node prevTail = tail;
        tail = new Node(prevTail, task, null);
        if (prevTail == null) {
            head = tail;
        } else {
            prevTail.next = tail;
        }
        nodes.put(task.getId(), tail);
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node node = head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
    }

    public static class Node {
        public Task data;
        public Node next;
        public Node prev;

        public Node(Node prev, Task data, Node next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}