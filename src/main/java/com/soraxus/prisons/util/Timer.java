package com.soraxus.prisons.util;

import java.util.ArrayList;
import java.util.List;

public class Timer {
    private long start;
    private long lastMark;
    private long end;

    List<Long> marks;

    public Timer() {
        reset();
    }

    public void stop() {
        this.end = System.nanoTime();
    }

    public void reset() {
        this.start = System.nanoTime();
        this.lastMark = this.start;
        this.end = -1;
        this.marks = new ArrayList<>();
    }

    public long mark() {
        long time = System.nanoTime() - lastMark;
        lastMark = System.nanoTime();
        marks.add(time);
        return time;
    }

    public long getTimeNanos() {
        return System.nanoTime() - this.start;
    }

    public long getTimeMillis() {
        return getTimeNanos() / 1000000;
    }

    public long getTimeTakenNanos() {
        return this.end - this.start;
    }

    public long getTimeTakenMillis() {
        return getTimeTakenNanos() / 1000000;
    }
}
