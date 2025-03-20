package tracker.history;

import tracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    public Node<Task> head;
    public Node<Task> tail;
    private Map<Integer, Node<Task>> nodes;

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
            Node<Task> node = nodes.get(taskID);
            nodes.remove(taskID);
            removeNode(node);
        }
    }

    public void removeNode(Node<Task> node) {
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
        Node<Task> node = nodes.get(task.getId());
        if (node != null) {
            nodes.remove(task.getId());
            removeNode(node);
        }

        Node<Task> prevTail = tail;
        tail = new Node<>(prevTail, task, null);
        if (prevTail == null) {
            head = tail;
        } else {
            prevTail.next = tail;
        }
        nodes.put(task.getId(), tail);
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> node = head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
