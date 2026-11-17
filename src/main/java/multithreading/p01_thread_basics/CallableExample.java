package multithreading.p01_thread_basics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/*
 * ==============================================================================
 * REVISION NOTES: Way 3 to Create Threads (Using java.util.concurrent.Callable)
 * ==============================================================================
 * 1. Return Values: Unlike `Runnable` (whose `run()` method returns `void`),
 *    `Callable<V>` defines a `call()` method that returns a generic result type `<V>`.
 * 2. Exception Handling: `Callable.call()` can throw checked exceptions,
 *    whereas `Runnable.run()` cannot declare checked exceptions.
 * 3. The Bridge (`FutureTask`): A standard `Thread` constructor cannot accept a
 *    `Callable` directly. `FutureTask` acts as an adapter/bridge because it implements
 *    both `Runnable` (so it can run on a Thread) and `Future` (so it can manage results).
 */
public class CallableExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("Main thread executing: " + Thread.currentThread().getName());

        // Step 1: Define the Callable task using a lambda expression
        Callable<Integer> callable = () -> {
            System.out.println("Calculating in worker thread: " + Thread.currentThread().getName());
            // Simulate some heavy work or computation
            Thread.sleep(1000);
            return 100 + 200;
        };

        /*
         * Step 2: Wrap the Callable inside a FutureTask
         * --------------------------------------------------------------------------
         * DESIGN PATTERN (Adapter Pattern):
         * FutureTask implements RunnableFuture,RunnableFuture  extends both Runnable and Future.
         * This allows the FutureTask to be passed to a standard Thread worker.
         */
        FutureTask<Integer> future = new FutureTask<>(callable);

        // Step 3: Pass FutureTask to Thread and start it
        Thread thread = new Thread(future);
        thread.start();

        /*
         * Step 4: Retrieve the result using future.get()
         * --------------------------------------------------------------------------
         * CRITICAL REVISION POINT:
         * - future.get() is a BLOCKING call. The main thread will pause here and wait
         *   until the worker thread completes execution and returns the result.
         * - If the worker thread throws an exception, future.get() wraps it in an
         *   ExecutionException and throws it to the calling thread.
         */
        System.out.println("Main thread waiting for result...");
        Integer result = future.get(); // Blocks until task completes

        System.out.println("Result: " + result);
        System.out.println("Main thread finished: " + Thread.currentThread().getName());
    }
}

/*
 * ==============================================================================
 * TOP INTERVIEW QUESTIONS & ANSWERS (CALLABLE & FUTURE)
 * ==============================================================================
 *
 * Q1: What are the main differences between Runnable and Callable?
 * A1:
 *   1. Return Type: Runnable's `run()` method returns `void`. Callable's `call()`
 *      method returns a parameterized result type (`<V>`).
 *   2. Exception Handling: Runnable's `run()` cannot throw checked exceptions.
 *      Callable's `call()` can throw checked exceptions (`throws Exception`).
 *   3. Usage: Runnable is used with traditional Threads and basic thread pools.
 *      Callable is typically used with `ExecutorService` and `Future` for asynchronous tasks.
 *
 * Q2: Can you pass a Callable directly to a java.lang.Thread constructor?
 * A2:
 *   - No. The `Thread` class only accepts `Runnable` or `ThreadGroup` targets.
 *   - To run a Callable on a raw Thread, you must wrap it in a `FutureTask` (which
 *     implements Runnable). Alternatively, you submit it directly to an `ExecutorService`
 *     via `executorService.submit(callable)`, which handles the Future creation internally.
 *
 * Q3: What is the behavior of the `future.get()` method?
 * A3:
 *   - It is a **blocking** call. If the background task has not finished executing yet,
 *     the calling thread blocks (waits) until the result becomes available.
 *   - There is also an overloaded version `future.get(long timeout, TimeUnit unit)`
 *     which waits only for a specified duration before throwing a `TimeoutException`.
 *
 * Q4: What happens if the code inside a Callable throws an exception? Does it crash the application?
 * A4:
 *   - No. The exception is caught and stored inside the `Future` object.
 *   - When you subsequently call `future.get()`, the stored exception is rethrown
 *     wrapped inside an `ExecutionException`.
 * ==============================================================================
 */