package multithreading.p01_thread_basics;

/*
 * ==============================================================================
 * REVISION NOTES: Way 2 to Create Threads (Implementing java.lang.Runnable)
 * ==============================================================================
 * 1. Separation of Concerns: The task (job logic in run()) is completely separated
 *    from the thread worker mechanism (the Thread class). MyRunnable only defines
 *    WHAT to do, while Thread defines WHO executes it (how it runs).
 * 2. Overcoming Inheritance Limits: Because Java supports multiple interface
 *    implementation, your class can still extend another base class if needed.
 * 3. Thread Pool Compatibility: Implementing Runnable is mandatory when working
 *    with ExecutorService and thread pools (e.g., Executors.newFixedThreadPool()),
 *    whereas extending Thread directly is not supported by thread pools.
 */
class MyRunnable implements Runnable {

    @Override
    public void run() {
        /*
         * The code inside run() represents the isolated job executed by the worker thread.
         * Thread.currentThread().getName() retrieves the name of the thread executing this task.
         */
        System.out.println("This is Runnable Thread " + Thread.currentThread().getName());
    }
}

public class ThreadRunnableExample {

    public static void main(String[] args) {

        System.out.println("Main thread executing: " + Thread.currentThread().getName());

        // Step 1: Instantiate the task via traditional class implementation
        MyRunnable myRunnable = new MyRunnable();

        /*
         * Step 2: Pass the Runnable instance to the Thread constructor
         * --------------------------------------------------------------------------
         * DESIGN PATTERN: This utilizes the Strategy / Delegation pattern.
         * The Thread object acts as the worker, and the Runnable object acts as the target task.
         * When thread.start() is called, the Thread delegates the execution to myRunnable.run().
         */
        Thread thread = new Thread(myRunnable);

        /*
         * MODERN JAVA (Java 8+): Lambda Expressions for Functional Interfaces
         * Since Runnable is a functional interface, we can pass a lambda expression
         * directly instead of creating a separate class or anonymous inner class.
         */
        Runnable myRunnable1 = () -> {
            System.out.println("Thread using lambda expression: " + Thread.currentThread().getName());
        };

        // Single-statement lambda shorthand (omits curly braces and return keyword)
        Runnable myRunnable2 = () -> System.out.println("Thread using lambda 2: " + Thread.currentThread().getName());

        Thread thread1 = new Thread(myRunnable1);
        // Passing an inline lambda directly into the Thread constructor
        Thread thread2 = new Thread(() -> System.out.println("Thread using inline lambda: " + Thread.currentThread().getName()));

        // Step 3: Start all threads
        thread1.start();
        thread2.start();
        thread.start();

        System.out.println("Main thread finished: " + Thread.currentThread().getName());
    }
}

/*
 * ==============================================================================
 * TOP INTERVIEW QUESTIONS & ANSWERS (RUNNABLE INTERFACE)
 * ==============================================================================
 *
 * Q1: Why is implementing 'Runnable' generally preferred over extending 'Thread'?
 * A1:
 *   1. Flexibility / Inheritance: Java does not support multiple class inheritance.
 *      If you extend Thread, your class cannot extend any other class. Implementing
 *      Runnable leaves your class inheritance slot open.
 *   2. Architecture / Separation of Concerns: It decouples the task (job logic)
 *      from the thread execution mechanism.
 *   3. Reusability: A single Runnable instance can be shared among multiple
 *      threads (useful for worker thread pools).
 *
 * Q2: Can you start a thread directly with a Runnable object without passing it
 *     to a Thread class instance?
 * A2:
 *   - No. The Runnable interface does not have a start() method; it only defines
 *     the abstract run() method.
 *   - Runnable represents the *task*, not the thread worker. To execute it concurrently,
 *     you must wrap it inside a java.lang.Thread instance (or submit it to an ExecutorService).
 *
 * Q3: Since Runnable is a Functional Interface, how can you instantiate it in
 *     modern Java (Java 8+) without writing a separate class?
 * A3:
 *   - You can use a Lambda Expression or Method Reference:
 *     Thread thread = new Thread(() -> System.out.println("Running via Lambda " + Thread.currentThread().getName()));
 *     thread.start();
 *
 * Q4: What happens if you call myRunnable.run() directly in the main method
 *     instead of creating a Thread?
 * A4:
 *   - It executes synchronously on the main thread as an ordinary method call.
 *     No new thread is spawned, exactly like calling thread.run() directly on a Thread subclass.
 * ==============================================================================
 */