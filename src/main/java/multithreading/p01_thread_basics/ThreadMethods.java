package multithreading.p01_thread_basics;

/*
 * ==============================================================================
 * REVISION NOTES: Essential Thread Methods in java.lang.Thread
 * ==============================================================================
 * 1. Thread Identification: `setName()` and `getName()` help distinguish threads 
 *    in logs and debugging (crucial for production microservices and monitoring).
 * 2. Liveness Check (`isAlive()`): Returns `true` if the thread has been started and 
 *    has not yet died (i.e., in RUNNABLE, BLOCKED, WAITING, or TIMED_WAITING state).
 * 3. Synchronization (`join()`): Forces the calling thread to wait until the target 
 *    thread completes execution.
 * 4. Pausing (`sleep()`): Temporarily halts the execution of the *current* thread 
 *    for a given duration without releasing any monitor locks it holds.
 */
public class ThreadMethods {

    public static void main(String[] args)
            throws InterruptedException {

        Thread worker = new Thread(() -> {

            // currentThread() returns a reference to the currently executing thread object
            System.out.println("Running: "
                    + Thread.currentThread().getName());

            try {
                // sleep() pauses the current thread and enters TIMED_WAITING state
                Thread.sleep(2000);

            } catch (InterruptedException e) {

                System.out.println("Worker interrupted");

                // Best Practice: Restore interrupt status flag after catching exception
                Thread.currentThread().interrupt();
            }

        });

        // setName() assigns a custom identifier to the thread for better traceability
        worker.setName("Payment-Worker");

        // getName() retrieves the assigned thread name
        System.out.println("Name: " + worker.getName());

        // isAlive() before start: Returns false because the thread is in the NEW state
        System.out.println("Alive: " + worker.isAlive());

        // start() spawns the OS-level thread and transitions it to RUNNABLE
        worker.start();

        // isAlive() after start: Returns true because the thread is now active/running
        System.out.println("Alive: " + worker.isAlive());

        // getState() inspects the current Thread.State enum value
        System.out.println("State: " + worker.getState());

        /*
         * join() - Wait for worker to complete
         * --------------------------------------------------------------------------
         * The main thread pauses here and blocks until `worker` finishes execution.
         */
        worker.join();

        System.out.println("Worker completed");

        // isAlive() after completion: Returns false because the thread is now TERMINATED
        System.out.println("Alive: " + worker.isAlive());
        System.out.println("Main Thread completed  "+ Thread.currentThread().getName());
    }
}

/*
 * ==============================================================================
 * TOP INTERVIEW QUESTIONS & ANSWERS (THREAD METHODS)
 * ==============================================================================
 *
 * Q1: What does `worker.isAlive()` return when a thread is in the `NEW` or `TERMINATED` state?
 * A1:
 *   - It returns `false` in both cases. `isAlive()` returns `true` only if the thread 
 *     has been started (`start()` was called) and has not yet died (i.e., it is alive 
 *     in the RUNNABLE, BLOCKED, WAITING, or TIMED_WAITING states).
 *
 * Q2: Does `Thread.sleep()` release the monitor locks acquired by the thread?
 * A2:
 *   - No! `Thread.sleep()` pauses execution but **keeps all locks/monitors** held by 
 *     the thread. This differs from `Object.wait()`, which releases the monitor lock 
 *     while waiting.
 *
 * Q3: What is the difference between `Thread.yield()` and `Thread.sleep()`?
 * A3:
 *   - `yield()`: Hints to the thread scheduler that the current thread is willing to 
 *     yield its current use of processor time, moving it back to the runnable state 
 *     to let other threads of equal priority run.
 *   - `sleep()`: Explicitly forces the thread to pause execution for a fixed, 
 *     specified amount of time regardless of other threads.
 *
 * Q4: Why is it important to name your worker threads (`worker.setName(...)`)?
 * A4:
 *   - Unnamed threads default to generic names like `Thread-0`, `Thread-1`, which make 
 *     debugging production thread dumps, logs, and monitoring tools (like Prometheus/Grafana) 
 *     extremely difficult. Descriptive names (e.g., `Payment-Worker`) instantly identify 
 *     the origin and purpose of background workloads.
 * ==============================================================================
 */