package multithreading.t01_fundamentals;

/** Illustrates independent tasks scheduled on separate threads. */
public class ConcurrencyVsParallelism {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 1; i <= 3; i++) {
                System.out.printf("%s -> %d%n", Thread.currentThread().getName(), i);
            }
        };
        Thread first = new Thread(task, "Task-A");
        Thread second = new Thread(task, "Task-B");
        first.start();
        second.start();
        first.join();
        second.join();
        System.out.println("Both tasks completed.");
    }
}
