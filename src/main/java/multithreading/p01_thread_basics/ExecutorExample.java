package multithreading.p01_thread_basics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * ==============================================================================
 * REVISION NOTES: Introduction to Executor Framework & Thread Pools
 * ==============================================================================
 * 1. Why Thread Pools?
 *    - Creating and destroying OS-level threads (`new Thread().start()`) is heavy
 *      in terms of CPU time and memory overhead.
 *    - An `ExecutorService` manages a pool of worker threads, reusing existing
 *      threads to execute multiple incoming asynchronous tasks, preventing thread exhaustion.
 * 2. Fixed Thread Pool (`newFixedThreadPool(n)`):
 *    - Creates a thread pool that reuses a fixed number of threads operating off a shared
 *      unbounded queue. At any point, at most `n` threads will be active processing tasks.
 *    - If all threads are busy and more tasks are submitted, they wait in the queue
 *      until a worker thread becomes available.
 */
public class ExecutorExample {

    public static void main(String[] args) {

        System.out.println("Main thread executing: " + Thread.currentThread().getName());

        // Step 1: Create a fixed thread pool with a capacity of 2 worker threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Step 2: Define tasks using modern Java lambda expressions (implementing Runnable)
        Runnable task1 = () ->
                System.out.println("Task 1 executed by worker: "
                        + Thread.currentThread().getName());

        Runnable task2 = () ->
                System.out.println("Task 2 executed by worker: "
                        + Thread.currentThread().getName());

        /*
         * Step 3: Submit tasks to the ExecutorService
         * --------------------------------------------------------------------------
         * - submit() places the task into the internal blocking queue of the executor.
         * - Since pool size is 2 and we have 2 tasks, both threads (e.g., pool-1-thread-1
         *   and pool-1-thread-2) will pick up and execute these tasks concurrently.
         */
        executor.submit(task1);
        executor.submit(task2);

        /*
         * Step 4: Graceful Shutdown
         * --------------------------------------------------------------------------
         * CRITICAL REVISION POINT:
         * - An `ExecutorService` maintains non-daemon threads. If you forget to call
         *   `shutdown()`, the JVM will keep running indefinitely because the thread
         *   pool threads remain alive waiting for new tasks.
         * - `shutdown()` initiates an orderly shutdown where previously submitted tasks
         *   are allowed to complete, but no new tasks are accepted.
         */
        executor.shutdown();

        System.out.println("Main thread finished submission: " + Thread.currentThread().getName());
    }
}

/*
 * ==============================================================================
 * TOP INTERVIEW QUESTIONS & ANSWERS (EXECUTOR SERVICE & THREAD POOLS)
 * ==============================================================================
 *
 * Q1: Why should you prefer ExecutorService over manually creating `new Thread()`?
 * A1:
 *   1. Resource Management: Avoids the heavy overhead of constantly creating and
 *      destroying thread instances by reusing worker threads.
 *   2. Throttling / Control: Prevents system instability and `OutOfMemoryError`
 *      by bounding the maximum number of concurrent threads (`newFixedThreadPool`).
 *   3. Abstraction: Decouples task submission from the mechanics of thread execution
 *      and scheduling.
 *
 * Q2: What is the difference between `executor.execute(Runnable)` and `executor.submit(Runnable/Callable)`?
 * A2:
 *   - `execute()`: Accepts only `Runnable`, returns `void`, and any unhandled
 *     exception thrown inside the task is logged or sent to the thread's uncaught exception handler.
 *   - `submit()`: Accepts both `Runnable` and `Callable`, returns a `Future` object
 *     which allows you to check task completion, retrieve return values, and catch
 *     checked exceptions wrapped in an `ExecutionException`.
 *
 * Q3: What happens if you do not call `executor.shutdown()` in your application?
 * A3:
 *   - The application will not terminate (hangs). The threads inside the thread pool
 *     are non-daemon threads and remain alive waiting for incoming tasks, keeping
 *     the JVM process active.
 *
 * Q4: What is the difference between `shutdown()` and `shutdownNow()`?
 * A4:
 *   - `shutdown()`: Initiates a graceful shutdown. It stops accepting new tasks
 *     but lets all currently running and queued tasks finish execution.
 *   - `shutdownNow()`: Attempts to stop all actively executing tasks immediately
 *     (via `Thread.interrupt()`) and halts the processing of waiting tasks, returning
 *     a list of tasks that were awaiting execution.
 * ==============================================================================
 */