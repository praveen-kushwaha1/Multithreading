# Java Multithreading Fundamentals (P01)

---

## 7. Thread Safety & Synchronization

### 7.2 What is a Race Condition?
A **race condition** occurs when the correctness of a program depends on the timing or ordering of concurrent operations.

#### Example:
```java
public class RaceConditionDemo {

    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter++;
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        // Wait for both threads to finish.
        thread1.join();
        thread2.join();

        System.out.println("Counter: " + counter);
    }
}
```
* **Expected result:** `2000`
* **Actual result:** Can be less than `2000`.

**Why?** Because `counter++` is a compound operation logically equivalent to:
1. Read `counter`.
2. Add `1`.
3. Write updated `counter`.

Two threads may read the same value simultaneously and overwrite each other's updates.

---

### 7.3 What is a Critical Section?
A **critical section** is a portion of code that accesses shared mutable data and must be protected against conflicting concurrent execution.

#### Example:
```java
synchronized (this) {
    counter++;
}
```
> The critical section is the code inside the `synchronized` block.

---

### 7.4 What is Synchronization?
Synchronization controls access to shared resources. Java provides several mechanisms:
1. `synchronized`
2. `volatile`
3. `Lock` and `ReentrantLock`
4. Atomic classes (`AtomicInteger`, etc.)
5. Concurrent collections (`ConcurrentHashMap`, etc.)

> **Note:** These mechanisms solve different problems; they are not interchangeable.

---

### 7.5 Method 1: `synchronized`
```java
public class SynchronizedDemo {

    private int counter = 0;

    // Protects access using the object's monitor lock.
    public synchronized void increment() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public static void main(String[] args) throws InterruptedException {

        SynchronizedDemo demo = new SynchronizedDemo();

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                demo.increment();
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println(demo.getCounter());
    }
}
```
* **Output:** `2000`
* **Why does this work?** Only one thread can hold the object's monitor lock and execute the `synchronized` instance method at a time.

---

### 7.6 Method 2: `volatile`
`volatile` guarantees **visibility** of updates between threads and establishes **memory-ordering** guarantees.

#### Example:
```java
public class VolatileDemo {

    // Changes made by one thread become visible to other threads
    // that access this volatile variable.
    private static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            while (running) {
                // Continue working while running is true.
            }
            System.out.println("Worker stopped");
        });

        worker.start();

        Thread.sleep(1000);
        running = false;

        worker.join();
    }
}
```
Without `volatile` (or another synchronization mechanism), the worker thread is not guaranteed to observe the updated value.

> **Important Interview Point:** `volatile` does **not** make compound operations atomic.
> ```java
> // This is NOT thread-safe:
> private volatile int counter = 0;
> 
> public void increment() {
>     counter++; // Still involves reading, modifying, and writing.
> }
> ```

---

### 7.7 Method 3: `AtomicInteger`
Atomic classes provide lock-free atomic operations using low-level CPU primitives such as **Compare-And-Swap (CAS)**.

#### Example:
```java
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicDemo {

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.incrementAndGet();
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println(counter.get());
    }
}
```
* **Output:** `2000`

---

### 7.8 Method 4: `ReentrantLock`
`ReentrantLock` provides explicit locking and additional features such as timed lock acquisition, fairness policies, and interruptible locking.

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {

    private final Lock lock = new ReentrantLock();
    private int counter = 0;

    public void increment() {
        // Acquire the lock before modifying shared data.
        lock.lock();
        try {
            counter++;
        } finally {
            // Always release the lock in a finally block.
            lock.unlock();
        }
    }

    public int getCounter() {
        return counter;
    }
}
```
> **Best Practice:** Always release a lock in a `finally` block to avoid accidentally leaving it locked if an exception occurs.

---

### 7.9 Thread Safety Techniques Comparison

| Technique | Main Purpose | Atomic Increment? |
| :--- | :--- | :--- |
| **`synchronized`** | Mutual exclusion and visibility | Yes (if increment is inside protected section) |
| **`volatile`** | Visibility and ordering | **No** |
| **`AtomicInteger`** | Atomic numeric operations | Yes |
| **`ReentrantLock`** | Explicit locking | Yes (if protected) |
| **`ConcurrentHashMap`** | Concurrent map operations | Individual supported operations are thread-safe |

---

### 7.10 Immutable Objects and Thread Safety
An **immutable object** cannot be modified after construction. They are inherently thread-safe and easy to share between threads because their state never changes.

#### Example:
```java
public final class Employee {

    private final int id;
    private final String name;

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```
**Characteristics of an Immutable Class:**
- Declared `final` (cannot be subclassed).
- All fields are `private` and `final`.
- No setter methods.
- No mutable internal state exposed (defensive copies are required if fields hold mutable objects).

---

### 7.11 Real-World Banking Example
Imagine a payment service handling concurrent requests on the same account:
* **Customer A** $\rightarrow$ Withdraw ₹5,000
* **Customer B** $\rightarrow$ Withdraw ₹3,000

**What a Lead Developer considers:**
1. Can both requests execute simultaneously?
2. Is the account balance shared?
3. Is the update atomic?
4. Is the database transaction correctly configured?
5. Could multiple application instances update the same account?
6. Are retries causing duplicate payments?

**Robust Banking Solution:**
- Database transactions with proper isolation levels.
- Conditional balance updates (Optimistic Locking) or row-level locking (Pessimistic Locking).
- Idempotency keys to prevent duplicate processing.
- Distributed locking/coordination across microservices.

> **Crucial Architecture Note:** A Java `synchronized` block only protects threads sharing that specific object inside **one JVM**. It cannot protect data across multiple JVM instances.

---

### 7.12 Lead Developer Interview Questions

* **Q1. What is the difference between a race condition and thread safety?**
  A race condition is a bug caused by uncontrolled concurrent execution. Thread safety is the design property of code that ensures correct behavior under concurrent access.
* **Q2. Why is `volatile` not enough for increment operations?**
  Because `counter++` is a compound read-modify-write operation. `volatile` guarantees visibility and ordering, not atomicity across multiple steps.
* **Q3. `synchronized` vs `ReentrantLock`?**
  Both provide mutual exclusion. `synchronized` uses intrinsic monitor locks and releases them automatically. `ReentrantLock` offers explicit control, timed/interruptible lock acquisition, fairness options, and multiple `Condition` variables.
* **Q4. How can you make a class thread-safe?**
  Use immutable objects where possible, minimize shared mutable state, protect critical sections with locks, and leverage atomic classes or concurrent collections.
* **Q5. Is `ConcurrentHashMap` completely thread-safe?**
  Yes, individual operations are thread-safe. However, compound "check-then-act" sequences (e.g., `get()` followed by `put()`) are not atomic unless you use atomic map methods like `computeIfAbsent()`.

---

## 8. Thread Lifecycle (`ThreadLifecycle.md`)

Understanding the Java thread lifecycle is essential before learning thread pools, locks, and executors.

### 8.1 What is Thread Lifecycle?
The thread lifecycle describes the states a thread transitions through from creation to termination, defined in `java.lang.Thread.State`:
1. `NEW`
2. `RUNNABLE`
3. `BLOCKED`
4. `WAITING`
5. `TIMED_WAITING`
6. `TERMINATED`

### 8.2 Thread Lifecycle Diagram
```text
          Thread Created
                |
                v
              NEW
                |
             start()
                |
                v
            RUNNABLE
                |
        +-------+--------+
        |       |        |
        v       v        v
     BLOCKED  WAITING  TIMED_WAITING
        |       |        |
        +-------+--------+
                |
                v
             RUNNABLE
                |
          run() completes
                |
                v
           TERMINATED
```

---

### 8.3 State 1: `NEW`
A thread is in the `NEW` state when instantiated, before `start()` is invoked.
```java
public class NewStateDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> System.out.println("Task running"));
        System.out.println(thread.getState()); // Output: NEW
    }
}
```

### 8.4 State 2: `RUNNABLE`
After calling `start()`, the thread enters `RUNNABLE`. This encompasses both **ready to run** and **currently executing** (Java does not define a separate `RUNNING` state).
```java
public class RunnableStateDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> System.out.println("Thread is executing"));
        thread.start();
        System.out.println(thread.getState()); // Likely RUNNABLE (unless already finished)
    }
}
```

### 8.5 State 3: `BLOCKED`
A thread enters `BLOCKED` when waiting to acquire an intrinsic monitor lock (`synchronized`) held by another thread.
```java
public class BlockedStateDemo {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Thread 2 acquired lock");
            }
        });

        thread1.start();
        Thread.sleep(200);
        thread2.start();
        Thread.sleep(200);

        System.out.println(thread2.getState()); // Output: BLOCKED
    }
}
```

### 8.6 State 4: `WAITING`
A thread enters `WAITING` when waiting indefinitely for another thread to perform a specific action.
* **Common triggers:** `Object.wait()`, `Thread.join()`, `LockSupport.park()`

```java
public class WaitingStateDemo {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        Thread worker = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait(); // Waits indefinitely until notified
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        worker.start();
        Thread.sleep(300);
        System.out.println(worker.getState()); // Output: WAITING

        synchronized (lock) {
            lock.notify();
        }
        worker.join();
    }
}
```

### 8.7 State 5: `TIMED_WAITING`
A thread enters `TIMED_WAITING` when waiting for a specified period.
* **Common triggers:** `Thread.sleep(ms)`, `Object.wait(ms)`, `Thread.join(ms)`, `LockSupport.parkNanos()`

```java
public class TimedWaitingDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        worker.start();
        Thread.sleep(200);
        System.out.println(worker.getState()); // Output: TIMED_WAITING

        worker.interrupt();
        worker.join();
    }
}
```

### 8.8 State 6: `TERMINATED`
A thread enters `TERMINATED` once `run()` completes normally or exits due to an uncaught exception.
```java
public class TerminatedStateDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> System.out.println("Task completed"));
        worker.start();
        worker.join();
        System.out.println(worker.getState()); // Output: TERMINATED
    }
}
```

---

### 8.9 All Six States Comparison

| State | Meaning | Trigger Example |
| :--- | :--- | :--- |
| **`NEW`** | Created, not yet started | `new Thread()` |
| **`RUNNABLE`** | Ready to run or actively executing | After `thread.start()` |
| **`BLOCKED`** | Waiting for a monitor lock | Entering a `synchronized` block |
| **`WAITING`** | Waiting indefinitely for a signal | `wait()`, `join()` |
| **`TIMED_WAITING`** | Waiting for a bounded duration | `sleep(1000)`, `wait(1000)` |
| **`TERMINATED`** | Execution has finished | `run()` completes |

---

### 8.10 Important Interview Differences

* **Q1. Difference between `start()` and `run()`?**
  `start()` allocates OS resources and spawns a new thread of execution, which then invokes `run()`. Calling `run()` directly executes the method synchronously on the *current* thread without creating a new thread.
* **Q2. Difference between `sleep()` and `wait()`?**

| Feature | `Thread.sleep()` | `Object.wait()` |
| :--- | :--- | :--- |
| **Defined in** | `java.lang.Thread` | `java.lang.Object` |
| **Lock behavior** | **Keeps** all held monitor locks | **Releases** the monitor lock on the target object |
| **Synchronization** | Can be called anywhere | Must be called inside a `synchronized` block/method |
| **Purpose** | Timed execution pause | Inter-thread communication/coordination |

* **Q3. Can a terminated thread be restarted?**
  No. Calling `start()` on a thread that has already been started throws `IllegalThreadStateException`.
* **Q4. What is `join()`?**
  `join()` causes the calling thread to wait until the target thread finishes execution.
* **Q5. What is `yield()`?**
  `Thread.yield()` is a hint to the OS thread scheduler that the current thread is willing to pause and let other threads of equal priority run. The scheduler is free to ignore this hint.

---

## 9. Java Memory Basics (`JavaMemoryBasics.md`)

### 9.1 JVM Memory Structure
For multithreading, the key JVM runtime data areas are:
1. **Heap Memory** (Shared)
2. **JVM Stack Memory** (Per-thread)
3. **Method Area / Metaspace** (Shared class metadata)
4. **Program Counter (PC) Register** (Per-thread)
5. **Native Method Stack** (Per-thread)

---

### 9.2 Heap Memory vs. Stack Memory

| Feature | Heap Memory | Stack Memory |
| :--- | :--- | :--- |
| **Purpose** | Stores objects and arrays | Stores method frames, local primitives, and object references |
| **Ownership** | Shared across all threads in the JVM | Isolated per thread |
| **Thread Safety** | Requires synchronization for mutable data | Inherently thread-safe (private to thread) |
| **Memory Management** | Garbage Collector (GC) | Automatic push/pop on method call/return |
| **Error on Exhaustion** | `OutOfMemoryError` | `StackOverflowError` |

#### Conceptual Memory Layout:
```text
                 JVM HEAP (Shared)
             ┌─────────────────────┐
             │   Account Object    │
             │   balance = 1000    │
             └─────────────────────┘
                        ▲
                        │
          ┌─────────────┴─────────────┐
          │                           │
   Thread 1 Stack              Thread 2 Stack
 ┌─────────────────┐         ┌─────────────────┐
 │ amount = 100    │         │ amount = 200    │
 │ account (ref)   │         │ account (ref)   │
 └─────────────────┘         └─────────────────┘
```

---

### 9.6 & 9.7 Java Memory Model (JMM) & The Visibility Problem
The **Java Memory Model (JMM)** defines rules governing how and when changes made by one thread become visible to other threads.

Without synchronization, CPU caches and compiler optimizations can cause a **visibility problem**, where a thread reads stale data instead of updates written by another thread. Declaring a variable `volatile` or using locks resolves this.

---

### 9.8 Happens-Before Relationships
**Happens-Before** is a JMM guarantee that memory writes by one specific statement are visible to another specific statement.

| Rule | Guarantee |
| :--- | :--- |
| **Program Order** | Earlier actions in a single thread happen-before later actions in that same thread. |
| **Monitor Lock** | Unlocking a monitor happens-before every subsequent lock on that same monitor. |
| **Volatile Variable** | A write to a `volatile` field happens-before every subsequent read of that field. |
| **Thread Start** | Actions before `thread.start()` happen-before any actions inside the started thread. |
| **Thread Join** | All actions in a thread happen-before another thread successfully returns from `thread.join()`. |

---

### 9.9 – 9.11 Context Switching & Memory Errors
* **Context Switching:** The OS saves the register/execution state of a running thread and loads another. Excessive threads cause high context-switching overhead, degrading throughput.
* **`StackOverflowError`:** Thrown when a thread's call stack exceeds its allocated size (typically due to deep/infinite recursion).
* **`OutOfMemoryError`:** Thrown when the JVM cannot allocate an object in the heap (or cannot create new native threads due to OS memory limits).

---

### 9.12 Memory & JMM Interview Questions
* **Q1. Does `volatile` force a variable to be stored only in main memory?**
  No, that is a common simplification. `volatile` enforces JMM **visibility and ordering guarantees** (via memory barriers/fences), ensuring CPU caches stay coherent without bypassing hardware caches entirely.
* **Q2. Why is heap memory not thread-safe?**
  Because all threads in a JVM share the same heap space and can concurrently mutate the same object fields without coordination.

---

## Final Revision Checklist — All 9 Fundamentals

| No. | Topic | Core Concepts to Master |
| :---: | :--- | :--- |
| **1** | Process vs Thread | Process isolation, JVM instance, lightweight threads |
| **2** | Concurrency vs Parallelism | Interleaved task progress vs simultaneous multi-core execution |
| **3** | Multithreading vs Multiprocessing | Shared heap memory vs separate process address spaces |
| **4** | Single Thread vs Multithreading | Sequential blocking execution vs concurrent responsiveness |
| **5** | CPU Core and Threads | Physical cores, hardware threads (Hyper-Threading), OS threads, JVM threads |
| **6** | Shared vs Local Resources | Heap objects vs Stack frames & `ThreadLocal` |
| **7** | Thread Safety Basics | Race conditions, critical sections, `synchronized`, `volatile`, `Atomic*` |
| **8** | Thread Lifecycle | `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED` |
| **9** | Java Memory Basics | Heap vs Stack, JMM, visibility, happens-before guarantees |

---

## Practical Exercises (0/12 Completed)

Implement these hands-on exercises before advancing to **P02: Thread Creation and Execution**:

- [ ] **1.** Create two threads by extending the `Thread` class.
- [ ] **2.** Execute the same task across multiple threads using `Runnable`.
- [ ] **3.** Print active thread names using `Thread.currentThread().getName()`.
- [ ] **4.** Create a shared counter and reproduce a race condition.
- [ ] **5.** Fix the race condition using `synchronized`.
- [ ] **6.** Fix the race condition using `AtomicInteger`.
- [ ] **7.** Demonstrate visibility using a `volatile` boolean stop flag.
- [ ] **8.** Demonstrate isolated per-thread state using `ThreadLocal` with two threads.
- [ ] **9.** Demonstrate `sleep()`, `join()`, and `interrupt()` handling.
- [ ] **10.** Programmatically print and observe all six `Thread.State` values.
- [ ] **11.** Create a shared `Account` object and perform thread-safe concurrent withdrawals.
- [ ] **12.** Sketch/explain Heap and Stack memory allocation for a multi-threaded program.

---

### Next Folder Roadmap: `p02_thread_creation_and_execution/`
```text
p02_thread_creation_and_execution/
├── ThreadClass.md
├── RunnableInterface.md
├── CallableInterface.md
├── ThreadVsRunnable.md
├── ThreadStartVsRun.md
├── ThreadSleepAndJoin.md
├── ThreadInterrupt.md
├── ThreadNamingAndPriority.md
├── DaemonThreads.md
└── ThreadUncaughtExceptionHandler.md
```