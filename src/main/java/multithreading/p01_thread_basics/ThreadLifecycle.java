package multithreading.p01_thread_basics;

/*
 * ==============================================================================
 * REVISION NOTES: Java Thread Lifecycle (java.lang.Thread.State)
 * ==============================================================================
 * Java threads pass through 6 distinct states defined in Thread.State enum:
 * 1. NEW: Thread object created via `new Thread()`, but `start()` not yet called.
 * 2. RUNNABLE: Executing in the JVM or waiting for CPU time from the OS scheduler.
 * 3. BLOCKED: Waiting to acquire a monitor lock to enter a synchronized block/method.
 * 4. WAITING: Waiting indefinitely for another thread (via wait(), join(), or park()).
 * 5. TIMED_WAITING: Waiting for a specified period (via sleep(millis), wait(timeout), join(timeout)).
 * 6. TERMINATED: Execution has completed normally or due to an unhandled exception.
 */
public class ThreadLifecycle {

    public static void main(String[] args)
            throws InterruptedException {

        Thread worker = new Thread(() -> {
            try {
                System.out.println("Worker started on thread: " + Thread.currentThread().getName());

                // Thread enters TIMED_WAITING state when sleep() is invoked
                Thread.sleep(2000);

                System.out.println("Worker completed");

            } catch (InterruptedException e) {
                // Best Practice: Restore the interrupted status flag
                Thread.currentThread().interrupt();
            }
        });

        // 1. NEW state: Thread is instantiated, OS-level thread not yet allocated
        System.out.println("Before start: " + worker.getState());

        // Start a new thread (Transitions state from NEW to RUNNABLE)
        worker.start();

        /*
         * Main thread sleeps briefly (200ms) to allow the worker thread 
         * enough time to reach its Thread.sleep(2000) statement.
         */
        Thread.sleep(200);

        // 2. TIMED_WAITING state: Worker is sleeping inside try block
        System.out.println("During execution: "
                + worker.getState());

        /*
         * Main thread waits for worker completion using join()
         * --------------------------------------------------------------------------
         * CRITICAL REVISION POINT:
         * - `worker.join()` causes the current thread (main) to wait until `worker` 
         *   dies (reaches TERMINATED state).
         * - Internally utilizes Object.wait() on the thread object monitor.
         */
        worker.join();

        // 3. TERMINATED state: Worker thread execution block has finished
        System.out.println("After completion: "
                + worker.getState());
    }
}

/*
 * ==============================================================================
 * TOP INTERVIEW QUESTIONS & ANSWERS (THREAD LIFECYCLE & STATES)
 * ==============================================================================
 *
 * Q1: How many thread states exist in Java, and what are they?
 * A1:
 *   There are 6 states defined in `java.lang.Thread.State`:
 *   NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, and TERMINATED.
 *   *(Note: Java treats ready-to-run and running as a single `RUNNABLE` state, 
 *   unlike OS-level thread state models which separate ready and running).*
 *
 * [ NEW ]
   │  start()
   ▼
[ RUNNABLE ] ◄────────────────────────┐
   │  ├── Waiting for monitor lock ──► [ BLOCKED ]
   │  ├── wait(), join() ────────────► [ WAITING ]
   │  └── sleep(ms), timed wait() ───► [ TIMED_WAITING ]
   │
   │  run() completes / terminates
   ▼
[ TERMINATED ]
 *
 * Q2: What is the difference between `WAITING` and `TIMED_WAITING` states?
 * A2:
 *   - `WAITING`: The thread is waiting indefinitely for another thread to perform 
 *     a specific action (e.g., calling `object.wait()` without timeout, or `thread.join()`).
 *   - `TIMED_WAITING`: The thread is waiting for a specified, bounded period of time 
 *     (e.g., `Thread.sleep(millis)`, `object.wait(timeout)`, or `thread.join(timeout)`).
 *
 * Q3: Can a terminated thread be restarted by calling `worker.start()` again?
 * A3:
 *   - No. Once a thread reaches the `TERMINATED` state, its life cycle is permanently 
 *     over. Calling `start()` on a dead thread throws a `java.lang.IllegalThreadStateException`.
 *
 * Q4: Why is `Thread.currentThread().interrupt()` called inside the catch block of `InterruptedException`?
 * A4:
 *   - When an `InterruptedException` is thrown, the JVM automatically clears (resets) 
 *     the thread's interrupted flag. Catching the exception and calling `interrupt()` 
 *     restores the flag so that upstream callers are aware the thread was interrupted.
 * ==============================================================================
 */