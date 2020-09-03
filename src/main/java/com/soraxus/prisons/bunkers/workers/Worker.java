package com.soraxus.prisons.bunkers.workers;

import com.soraxus.prisons.bunkers.base.elements.ElementWorkerHut;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

public class Worker implements GravSerializable {
    @Getter
    private ElementWorkerHut hut;
    private Task task;

    public Worker(GravSerializer serializer, ElementWorkerHut hut) {
        this.task = (Task) serializer.readObject(hut.getBunker(), this);
        this.hut = hut;
    }

    public Worker(ElementWorkerHut hut) {
        this.task = null;
        this.hut = hut;
    }

    public synchronized Task getTask() {
        if (task != null && task.isFinished())
            task = null;
        return task;
    }

    /**
     * Sets this worker's task and starts the task
     */
    public synchronized void setTask(Task task) {
        if (task.isFinished())
            return;
        this.task = task;
        if (!task.isStarted())
            task.start();
    }

    public int getSpeed() {
        return this.hut.getLevel() * 2;
    }

    public synchronized boolean isWorking() {
        return getTask() != null;
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(task);
    }
}
