package com.soraxus.prisons.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class MultiReentrantLock<T> {
    private final Map<T, ReentrantLock> map = new HashMap<>();
    private final Map<T, Integer> waitList = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    private int getWaitNum(T key) {
        lock.lock();
        if (!waitList.containsKey(key)) {
            lock.unlock();
            return 0;
        }
        int res = waitList.get(key);
        lock.unlock();
        return res;
    }

    private void incrementWaitNum(T key) {
        lock.lock();
        waitList.put(key, getWaitNum(key) + 1);
        lock.unlock();
    }

    private int decrementWaitNum(T key) {
        lock.lock();
        waitList.put(key, getWaitNum(key) - 1);
        int now = getWaitNum(key);
        if (now <= 0)
            waitList.remove(key);
        lock.unlock();
        return now;
    }

    /**
     * This is an empty javadoc, oh wait, not anymore
     *
     * @param id It's called id, so guess what it is...
     */
    public void lock(T id) {
        lock.lock();
        if (map.containsKey(id)) {
            ReentrantLock lock1 = map.get(id);
            incrementWaitNum(id);
            lock.unlock();
            lock1.lock();
            return;
        }
        map.put(id, new ReentrantLock(true));
        incrementWaitNum(id);
        map.get(id).lock();
        lock.unlock();
    }

    public void unlock(T id) {
        lock.lock();
        int now = decrementWaitNum(id);
        if (now <= 0) {
            map.remove(id).unlock();
        } else {
            map.get(id).unlock();
        }
        lock.unlock();
    }

    public boolean isLocked(T id) {
        lock.lock();
        boolean acquired = map.containsKey(id);
        lock.unlock();
        return acquired;
    }
}
