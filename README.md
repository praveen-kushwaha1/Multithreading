# Java Multithreading — Runnable Learning Lab

A sequenced, hands-on Java 21 Maven project for learning multithreading from fundamentals through advanced concurrency.

## Open and run in IntelliJ IDEA
1. Extract this ZIP.
2. IntelliJ IDEA → **Open** → select the `Java-Multithreading` folder.
3. Set Project SDK to **Java 21** (File → Project Structure → Project SDK).
4. Allow IntelliJ to import the Maven project.
5. Open any example class under `src/main/java` and click the green Run icon beside `main()`.

You can also run from a terminal:
```bash
mvn clean compile
```
To run a specific class:
```bash
mvn exec:java -Dexec.mainClass="com.learning.multithreading.topic02_thread_creation.ImplementingRunnable"
```
(The exec plugin may need Maven to download it the first time.)

## Learning sequence
1. Fundamentals
2. Thread creation
3. Thread lifecycle
4. Synchronization and thread safety
5. Inter-thread communication
6. Locks and synchronizers
7. Deadlock and concurrency problems
8. Executor framework
9. Callable, Future, CompletableFuture
10. Concurrent collections
11. Atomic classes and volatile
12. Java Memory Model
13. ThreadLocal
14. ForkJoin and parallel streams
15. Virtual threads (Java 21)
16. Concurrency patterns
17. Performance
18. Practical mini-projects

## Practice approach
For each example: run it, observe output, change the code, and explain why the result changes. Thread output order is often nondeterministic.

**Safety note:** `DeadlockExample` intentionally demonstrates deadlock and may not terminate. Run it only when you understand how to stop the process.
# Multithreading
# Multithreading
