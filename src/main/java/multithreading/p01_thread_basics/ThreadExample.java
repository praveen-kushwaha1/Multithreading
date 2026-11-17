package multithreading.p01_thread_basics;

/*
 * ==============================================================================
 * REVISION NOTES: Way 1 to Create Threads (Extending java.lang.Thread)
 * ==============================================================================
 * 1. Inheritance Limit: Since Java does not support multiple class inheritance,
 *    extending the Thread class prevents your class from extending any other class.
 * 2. Separation of Concerns: This approach tightly couples the task (job to be done)
 *    with the thread worker itself. Implementing Runnable is generally preferred.
 */
class MyThread extends Thread {

    @Override
    public void run() {
        /*
         * The code inside run() represents the job executed by the newly spawned thread.
         * Thread.currentThread().getName() retrieves the name of the worker thread
         * executing this block (e.g., "Thread-0").
         */
        System.out.println("My Thread is running: " + Thread.currentThread().getName());
    }
}

public class ThreadExample {

    public static void main(String[] args) {

        System.out.println("Main thread executing: " + Thread.currentThread().getName());

        // Step 1: Instantiate the custom thread (Thread is currently in the NEW state)
        MyThread thread = new MyThread();

        /*
         * Step 2: Invoke start()
         * Registers the thread with the JVM Thread Scheduler, allocates a new separate
         * call stack, and transitions the thread from NEW to RUNNABLE. Once scheduled,
         * the JVM invokes run() asynchronously.
         */
        thread.start();

        // Main thread continues its execution concurrently
        System.out.println("Main thread finished: " + Thread.currentThread().getName());
    }
}

/*
 * ==============================================================================
 * TOP INTERVIEW QUESTIONS & ANSWERS (THREAD BASICS)
 * ==============================================================================
 *
 * Q1: Why is implementing 'Runnable' preferred over extending 'Thread'?
 * A1:
 *   1. Multiple Inheritance Limit: Java does not support multiple class inheritance.
 *      Extending 'Thread' prevents your class from extending any other base class.
 *   2. Separation of Concerns: Extending 'Thread' tightly couples the task
 *      (the job inside run()) with the worker thread itself. Implementing 'Runnable'
 *      decouples the task from the execution mechanism (and works seamlessly with
 *      ExecutorService / thread pools).
 *
 * Q2: What happens if you call thread.run() directly instead of thread.start()?
 * A2:
 *   - No new thread is created. The JVM treats run() as an ordinary, synchronous
 *     method call.
 *   - It executes entirely on the calling thread (e.g., the 'main' thread),
 *     completely defeating multithreading.
 *   - Only thread.start() handles OS-level thread creation, stack allocation,
 *     and transitions the thread from NEW to RUNNABLE.
 *
 * Q3: Can you call thread.start() twice on the same thread instance?
 * A3:
 *   - No. Once a thread has started, calling start() a second time on the same
 *     instance throws a java.lang.IllegalThreadStateException at runtime, even if
 *     the thread has already finished execution (TERMINATED state).
 *
 * Q4: Is the execution order of 'main' and 'MyThread' guaranteed here?
 * A4:
 *   - No. Once thread.start() is called, execution order depends entirely on the
 *     JVM Thread Scheduler and OS thread scheduling. "Main thread finished" may
 *     print before, after, or concurrently with "My Thread is running".
 * ==============================================================================
 */