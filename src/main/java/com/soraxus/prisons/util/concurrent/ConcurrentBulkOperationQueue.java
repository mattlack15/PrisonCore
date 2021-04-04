package com.soraxus.prisons.util.concurrent;

import com.soraxus.prisons.util.list.LockingList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConcurrentBulkOperationQueue<T> {

    private final LockingList<T> queue = new LockingList();

    private final Consumer<List<T>> operation;

    public ConcurrentBulkOperationQueue(Consumer<List<T>> operation) {
        this.operation = operation;
    }

    public void queue(T obj) {
        queue.getLock().perform(() -> {
            if(!queue.contains(obj))
                queue.add(obj);
        });
    }

    public void flushQueue() {
        List<T> copy = new ArrayList<>();
        queue.getLock().perform(() -> {
            copy.addAll(queue);
            queue.clear();
        });
        operation.accept(copy);
    }
}
