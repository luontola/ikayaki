/*
 * LastExecutor.java
 *
 * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
 *
 * This file is part of Ikayaki.
 *
 * Ikayaki is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Ikayaki is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ikayaki; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ikayaki.util;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Executes the last Runnable tasks of a series of tasks after a delay. The worker thread will terminate automatically
 * when there are no runnables to be executed. Optionally executes all of the tasks and not only the last one. All
 * operations are thread-safe.
 * <p/>
 * This class can be used for example in connection with a "continuous search" invoked by a series of GUI events (such
 * as a DocumentListener), but it is necessary to react to only the last event after a short period of user inactivity.
 *
 * @author Esko Luontola
 */
public class LastExecutor implements Executor {

    /**
     * Defines how long is the delay in milliseconds, after which the events need to be run.
     */
    private int delayMillis;

    /**
     * Defines if only the last event should be executed. If false, then all of the events are executed in the order of
     * appearance.
     */
    private boolean execOnlyLast;

    /**
     * Prioritized FIFO queue for containing the RunDelayed items that have not expired. If execOnlyLast is true, then
     * this queue should never contain more than one item.
     */
    private DelayQueue<RunDelayed> queue = new DelayQueue<RunDelayed>();

    /**
     * The worker thread that will run the inserted runnables. If the thread has no more work to do, it will set
     * workerThread to null and terminate itself.
     */
    private Thread workerThread = null;

    /**
     * Creates an empty LastExecutor with a delay of 0 and execOnlyLast set to true.
     */
    public LastExecutor() {
        this(0, true);
    }

    /**
     * Creates an empty LastExecutor with execOnlyLast set to true.
     *
     * @param delayMillis the length of execution delay in milliseconds; if less than 0, then 0 will be used.
     */
    public LastExecutor(int delayMillis) {
        this(delayMillis, true);
    }

    /**
     * Creates an empty LastExecutor with a delay of 0.
     *
     * @param execOnlyLast if true, only the last event will be executed after the delay; otherwise all are executed in
     *                     order of appearance.
     */
    public LastExecutor(boolean execOnlyLast) {
        this(0, execOnlyLast);
    }

    /**
     * Creates an empty LastExecutor.
     *
     * @param delayMillis  the length of execution delay in milliseconds; if less than 0, then 0 will be used.
     * @param execOnlyLast if true, only the last task will be executed after the delay; otherwise all are executed in
     *                     order of appearance.
     */
    public LastExecutor(int delayMillis, boolean execOnlyLast) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        this.delayMillis = delayMillis;
        this.execOnlyLast = execOnlyLast;
    }

    /**
     * @return true if only the last task will be executed after the delay; otherwise false.
     */
    public synchronized boolean isExecOnlyLast() {
        return execOnlyLast;
    }

    /**
     * @param execOnlyLast if true, only the last task will be executed after the delay; otherwise all are executed in
     *                     order of appearance.
     */
    public synchronized void setExecOnlyLast(boolean execOnlyLast) {
        this.execOnlyLast = execOnlyLast;
    }

    /**
     * @return the delay in milliseconds.
     */
    public synchronized int getDelayMillis() {
        return delayMillis;
    }

    /**
     * @param delayMillis delay in milliseconds; if less than 0, then the new value is ignored.
     */
    public synchronized void setDelayMillis(int delayMillis) {
        if (delayMillis >= 0) {
            this.delayMillis = delayMillis;
        }
    }

    /**
     * Inserts a runnable task to the end of the queue. It will remain there until it is executed or another object
     * replaces it. If execOnlyLast is set to true, the queue will be cleared before inserting this runnable to it. If
     * there is no worker thread running, a new one will be spawned.
     *
     * @param command the runnable task to be executed after a pre-defined delay.
     * @throws NullPointerException if command is null.
     */
    public synchronized void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        if (execOnlyLast) {
            queue.clear();
        }
        queue.offer(new RunDelayed(command, delayMillis)); // always successful
        if (workerThread == null) {
            workerThread = new LastExecutorThread();
            workerThread.start();
        }
    }

    /**
     * Waits for the queue to become empty.
     *
     * @throws InterruptedException if another thread has interrupted the current thread. The interrupted status of the
     *                              current thread is cleared when this exception is thrown.
     */
    public synchronized void join() throws InterruptedException {
        while (workerThread != null) {
            wait();
        }
    }

    /**
     * Removes all of the elements from the execution queue. The queue will be empty after this call returns. The
     * execution thread will stop after the currently running task, if any.
     */
    public synchronized void clear() {
        queue.clear();
        if (workerThread != null) {
            // run an empty task, after which the execution thread will stop
            queue.offer(new RunDelayed(new Runnable() {
                public void run() {
                    // DO NOTHING
                }
            }, 0));
        }
    }

    /**
     * Keeps on checking the LastExecutor.queue to see if there are Runnables to be executed. If there is one, execute
     * it and proceed to the next one. If an uncaught Throwable is thrown during the execution, prints an error message
     * and stack trace to stderr. If the queue is empty, this thread will set LastExecutor.workerThread to null and
     * terminate itself.
     *
     * @author Esko Luontola
     */
    private class LastExecutorThread extends Thread {
        public void run() {
            while (true) {
                synchronized (LastExecutor.this) {
                    if (queue.size() == 0) {
                        workerThread = null;
                        LastExecutor.this.notifyAll(); // notify all who wait in LastExecutor.join()
                        return;
                    }
                }
                RunDelayed delayed = null;
                try {
                    delayed = queue.take();
                    delayed.getRunnable().run();
                } catch (Throwable t) {
                    System.err.println(t.getClass().getSimpleName() + " thrown by "
                            + delayed.getRunnable().getClass().getName());
                    t.printStackTrace();
                }
            }
        }
    }

    /**
     * Wraps a Runnable object and sets the delay after which it should be executed by a worker thread.
     *
     * @author Esko Luontola
     */
    private class RunDelayed implements Delayed {

        /**
         * The point in time when this RunDelayed will expire.
         */
        private long expires;

        /**
         * Contained Runnable object to be run after this RunDelayed has expired.
         */
        private Runnable runnable;

        /**
         * Creates a new RunDelayed item that contains runnable.
         *
         * @param runnable    the Runnable to be contained
         * @param delayMillis delay in milliseconds
         */
        public RunDelayed(Runnable runnable, int delayMillis) {
            this.expires = System.currentTimeMillis() + delayMillis;
            this.runnable = runnable;
        }

        /**
         * Returns the remaining delay associated with this object, always in milliseconds.
         *
         * @param unit ignored; always assumed TimeUnit.MILLISECONDS
         * @return the remaining delay; zero or negative values indicate that the delay has already elapsed
         */
        public long getDelay(TimeUnit unit) {
            return expires - System.currentTimeMillis();
        }

        /**
         * Returns the contained Runnable.
         *
         * @return the Runnable given as constructor parameter
         */
        public Runnable getRunnable() {
            return runnable;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.
         *
         * @param delayed the Delayed to be compared.
         * @return a negative integer, zero, or a positive integer as this delay is less than, equal to, or greater than
         *         the specified delay.
         */
        public int compareTo(Delayed delayed) {
            return (int) (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * TEST METHOD
     */
    public static void main(String[] args) throws InterruptedException {
        LastExecutor q = new LastExecutor(200, true);

        for (int i = 0; i < 10; i++) {
            final int j = i;
            q.execute(new Runnable() {
                public void run() {
                    System.out.println("A " + j);
                }
            });
        }

        q.join();

        for (int i = 0; i < 10; i++) {
            final int j = i;
            q.execute(new Runnable() {
                public void run() {
                    System.out.println("B " + j);
                }
            });
            Thread.sleep(50 * i);
        }

        // test that the LastExecutor will catch the Exception
        // and the same thread will continue to execute the next Runnable
        q.execute(new Runnable() {
            public void run() {
                System.out.println("C");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new NullPointerException();
            }
        });
        Thread.sleep(300);
        q.execute(new Runnable() {
            public void run() {
                System.out.println("D");
            }
        });
        q.join();

        // test that the clear() method stops the execution thread cleanly
        System.out.println("1");
        q.execute(new Runnable() {
            public void run() {
                System.out.println("A");
            }
        });
        Thread.sleep(100);
        System.out.println("2");
        q.clear();
        System.out.println("3");
    }
}
