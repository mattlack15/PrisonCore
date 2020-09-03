package com.soraxus.prisons.util.locks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

//This one is slightly faster on average than the spinning lock
public class CustomReentrantLockLooper {

    public static class Node {
        public final AtomicReference<Node> next = new AtomicReference<>(); //I realize now this doesn't have to be atomic
        public final Thread thread;

        public Node(Thread thread, Node next) {
            this.next.set(next);
            this.thread = thread;
        }
    }

    private final AtomicReference<Thread> owner = new AtomicReference<>();
    private final Node waitList = new Node(null, null);
    private final AtomicInteger lockCount = new AtomicInteger();

    public void lock() {
        if (!owner.compareAndSet(null, Thread.currentThread())) {

            if (owner.get() == Thread.currentThread()) {
                lockCount.incrementAndGet(); //Already acquired the lock
                return;
            }

            //Unable to acquire lock
            //Wait and park thread

            Node ourNode = new Node(Thread.currentThread(), null);
            Node current = waitList;

            //NOTE: I would rather have a loop here and add to the end of the list here instead of
            //waking threads from the end of the list when unlocking because at this point this thread
            //is probably going to be waiting anyways, so might as well spend that time lifting some weight
            Node parent = waitList;
            while (!current.next.compareAndSet(null, ourNode)) {
                Node n = current.next.get();
                current = n != null ? n : current; //Check if the value changed between the compareAndSet and get
                parent = n != null ? current : parent;
            }

            //We've added ourselves to the wait list

            while (!owner.compareAndSet(null, Thread.currentThread())) {
                LockSupport.park(this);
            }

            //Lock acquired

            //Unlink ourselves from the wait list
            //No nodes can be removed since we have the lock so we can safely search the tree
            //Nodes can be added however
            current = waitList.next.get();
            parent = waitList;
            for (; current != null && current != ourNode; current = current.next.get()) {
                parent = current;
            }

            //Unlink
            Node after = ourNode.next.getAndSet(parent); //Make it loop around to our parent node
            //The looping is so that any threads trying to add themselves to the wait list will loop around
            //in a circle until we cut the loop by unlinking ourselves fully from the wait list
            parent.next.set(after); //Cut the loop by removing our node from the linked list
            //Done
        }
        lockCount.incrementAndGet(); //Increment lock count
    }

    public void unlock() {
        if (owner.get() == Thread.currentThread()) {
            if (lockCount.decrementAndGet() == 0) {
                //Release lock
                owner.set(null);
                //Wake up the waiting thread at the front of the queue
                Node node = waitList.next.get();
                if (node != null) {
                    LockSupport.unpark(node.thread);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int[] counter = {0};
        CustomReentrantLockLooper lock = new CustomReentrantLockLooper();
        AtomicLong ns = new AtomicLong(0L);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                long n = System.nanoTime();
                lock.lock();
                counter[0]++;
                lock.unlock();
                ns.getAndAdd(System.nanoTime() - n);
            }).start();
        }
        Thread.sleep(1000);
        System.out.println("Average: " + (ns.get() / 1000) + "ns completed: " + counter[0]);
    }
}
