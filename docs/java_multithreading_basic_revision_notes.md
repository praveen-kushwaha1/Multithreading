# ☕ Java Multithreading: Revision Notes

In Java, a **thread** is a lightweight unit of execution that allows a program to perform multiple tasks concurrently.

---

## Part 1: Thread Creation in Java

For interviews and real-world development, focus on these primary approaches:
1. Extending the `Thread` class
2. Implementing the `Runnable` interface (and Lambda expressions)
3. Implementing the `Callable` interface
4. Using `ExecutorService` *(Recommended for enterprise applications)*

---

### 1. Extending the `Thread` Class
Create a class that extends `java.lang.Thread` and override its `run()` method.

```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread is running: " + Thread.currentThread().getName());
    }
}

public class ThreadExample {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        t1.start(); // Starts a new thread
    }
}
```
**Output:** `Thread is running: Thread-0`

* **How it works:** `start()` allocates a new call stack and tells the JVM to execute `run()` in a separate thread.
* **Interview Trap:** Calling `t1.run()` directly executes the method on the *current* (main) thread—it does **not** create a new thread.

---

### 2. Implementing the `Runnable` Interface
Generally preferred over extending `Thread` because Java does not support multiple class inheritance. Implementing `Runnable` allows your class to extend another class if needed and separates the **task** from the **thread runner**.

```java
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Task executing: " + Thread.currentThread().getName());
    }
}

public class RunnableExample {
    public static void main(String[] args) {
        MyTask task = new MyTask();
        Thread t1 = new Thread(task);
        t1.start();
    }
}
```

#### Real-World Example: Async Email Notification
```java
class EmailTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Sending transaction email...");
    }
}

public class BankingApp {
    public static void main(String[] args) {
        Thread emailThread = new Thread(new EmailTask());
        emailThread.start();
        System.out.println("Transaction completed");
    }
}
```
> **Note:** This demonstrates *concurrency*, not execution order. The email task may finish before or after `"Transaction completed"` is printed.

---

### 3. Using Lambda Expressions
Since `Runnable` is a `@FunctionalInterface`, you can write concise inline tasks without creating separate classes.

```java
public class LambdaThreadExample {
    public static void main(String[] args) {
        // Explicit Runnable
        Runnable task = () -> System.out.println("Running in: " + Thread.currentThread().getName());
        new Thread(task).start();

        // One-liner
        new Thread(() -> System.out.println("Hello from thread")).start();
    }
}
```

---

### 4. Implementing the `Callable` Interface
Unlike `Runnable`, `Callable<V>` can **return a result** and **throw checked exceptions**. It is paired with `FutureTask` or an `ExecutorService`.

```java
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class CallableExample {
    public static void main(String[] args) throws Exception {
        Callable<Integer> task = () -> {
            System.out.println("Calculating...");
            return 100 + 200;
        };

        FutureTask<Integer> futureTask = new FutureTask<>(task);
        Thread t1 = new Thread(futureTask);
        t1.start();

        Integer result = futureTask.get(); // Blocks until result is ready
        System.out.println("Result: " + result);
    }
}
```
**Output:**
```text
Calculating...
Result: 300
```

---

### 5. Using `ExecutorService` (Enterprise Standard)
Manually creating threads is expensive. In production (e.g., Spring Boot), we use `ExecutorService` to manage a **pool of reusable worker threads**.

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable task1 = () -> System.out.println("Task 1: " + Thread.currentThread().getName());
        Runnable task2 = () -> System.out.println("Task 2: " + Thread.currentThread().getName());

        executor.submit(task1);
        executor.submit(task2);

        executor.shutdown(); // Stops accepting new tasks; finishes existing ones
    }
}
```

---

### 📊 Comparison of Approaches

| Approach | Returns Result? | Throws Checked Exception? | Primary Use Case |
| :--- | :---: | :---: | :--- |
| **Extend `Thread`** | ❌ No | ❌ No | Basic learning / simple overrides |
| **Implement `Runnable`** | ❌ No | ❌ No | Standard task definition (decoupled) |
| **Lambda `Runnable`** | ❌ No | ❌ No | Quick, short inline tasks |
| **Implement `Callable`** | ✅ Yes | ✅ Yes | Tasks requiring return values |
| **`ExecutorService`** | ✅ Depends on task | ✅ Yes (via `Future`) | Production / Enterprise thread pooling |

---

## Part 2: Thread Lifecycle & States

Java defines **6 thread states** in the `Thread.State` enum:

```text
[ NEW ] 
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
```

1. **`NEW`**: Thread object created, `start()` not yet called.
2. **`RUNNABLE`**: Ready to run or currently executing on the CPU. *(Note: Java does **not** have a separate `RUNNING` state).*
3. **`BLOCKED`**: Waiting to acquire a monitor lock (e.g., entering a `synchronized` block).
4. **`WAITING`**: Waiting indefinitely for another thread to perform an action (`Object.wait()`, `Thread.join()`).
5. **`TIMED_WAITING`**: Waiting for a specified period (`Thread.sleep(ms)`, `wait(ms)`, `join(ms)`).
6. **`TERMINATED`**: Execution finished or aborted due to an unhandled exception.

### Lifecycle Demonstration (`ThreadLifecycle.java`)
```java
public class ThreadLifecycle {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                System.out.println("Worker started");
                Thread.sleep(2000);
                System.out.println("Worker completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("Before start: " + worker.getState()); // NEW
        worker.start();

        Thread.sleep(200);
        System.out.println("During execution: " + worker.getState()); // TIMED_WAITING

        worker.join(); // Main thread waits for worker
        System.out.println("After completion: " + worker.getState()); // TERMINATED
    }
}
```

---

## Part 3: Important Thread Methods

| Method | Description |
| :--- | :--- |
| `start()` | Allocates resources and invokes `run()` in a new thread. |
| `run()` | Contains the code/task to be executed. |
| `sleep(long ms)` | Pauses the **currently executing** thread for `ms` milliseconds. |
| `join()` | Forces the calling thread to wait until the target thread dies. |
| `yield()` | Hints to the thread scheduler that the current thread is willing to pause. |
| `interrupt()` | Sets the interrupt flag on a thread (requests graceful stop). |
| `isInterrupted()` | Checks interrupt status *without* clearing the flag. |
| `interrupted()` | Static method; checks interrupt status *and clears* the flag. |
| `isAlive()` | Returns `true` if the thread has started and not yet terminated. |
| `getName()` / `setName()` | Gets or sets the thread's name (crucial for debugging logs). |
| `getState()` | Returns the current `Thread.State`. |
| `currentThread()` | Static method returning a reference to the currently executing thread. |

### Deep Dive: Key Method Comparisons

#### 1. `sleep()` vs. `join()`
* **`sleep(ms)`**: Pauses the *current* thread for a fixed time. Does **not** release held locks.
* **`join()`**: Pauses the *calling* thread until *another* thread completes. Does **not** release held locks.

#### 2. Graceful Interruption (`interrupt()`)
`interrupt()` does not forcibly kill a thread; it sets a flag. If the thread is in `sleep()` or `wait()`, it throws `InterruptedException` and clears the flag.
```java
Thread worker = new Thread(() -> {
    try {
        Thread.sleep(10000);
    } catch (InterruptedException e) {
        System.out.println("Stop requested");
        Thread.currentThread().interrupt(); // Best practice: restore interrupt status
    }
});
worker.start();
worker.interrupt();
```

---

## Part 4: Interview Question Bank

### Fundamentals
* **Q1: What is the difference between `start()` and `run()`?**
  * `start()` creates a new OS/JVM thread and invokes `run()` asynchronously. Calling `run()` directly runs synchronously on the current thread.
* **Q2: Can we call `start()` twice on the same thread instance?**
  * No. It throws `IllegalThreadStateException`. A terminated thread cannot be restarted.
* **Q3: What is the difference between `Runnable` and `Callable`?**
  * `Runnable.run()` returns `void` and cannot throw checked exceptions. `Callable.call()` returns a generic type `V` and can throw checked exceptions.

### Lead-Level / Lifecycle Questions
* **Q4: Why doesn't Java have a separate `RUNNING` state?**
  * Because modern OS schedulers switch threads in and out of CPU cores in microseconds. Distinguishing "ready" vs. "on CPU" at the JVM level adds overhead with little practical value, so both are grouped under `RUNNABLE`.
* **Q5: `BLOCKED` vs. `WAITING`?**
  * `BLOCKED` occurs passively when trying to enter a `synchronized` block/method held by another thread. `WAITING` occurs explicitly when calling `wait()` or `join()`.
* **Q6: `sleep()` vs. `wait()`?**
  * `sleep()` is a static `Thread` method that **keeps locks**. `wait()` is an `Object` method that **releases the monitor lock** and must be called inside a `synchronized` context.
* **Q7: `interrupted()` vs. `isInterrupted()`?**
  * `Thread.interrupted()` is static and resets the interrupt flag to `false`. `t.isInterrupted()` is an instance method and leaves the flag unchanged.

---

## Part 5: Practical Action Plan

### Practice Checklist
- [ ] Create a thread by extending `Thread` and print its name.
- [ ] Create a thread using `Runnable` and pass it to `Thread`.
- [ ] Create a thread using a Lambda expression.
- [ ] Run two threads concurrently executing different tasks.
- [ ] Compare `t.start()` vs `t.run()` by printing `Thread.currentThread().getName()`.
- [ ] Create a `Callable` task that returns the sum of two numbers via `FutureTask`.
- [ ] Create an `ExecutorService` with 2 threads to run 5 tasks.
- [ ] Use `shutdown()` and `awaitTermination()` to wait for pool completion.

### Recommended Repository Structure
```text
01_fundamentals/
├── ThreadCreation.java
├── ThreadLifecycle.java
├── ThreadMethods.java
├── ThreadPriority.java
├── DaemonThread.java
├── ThreadNaming.java
└── InterruptHandling.java
```