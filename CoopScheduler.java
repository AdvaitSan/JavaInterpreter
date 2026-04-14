import java.util.LinkedList;
import java.util.Queue;

public class CoopScheduler {

    private final Queue<CoroTask> ready = new LinkedList<>();

    public void submit(CoroTask task) {
        ready.add(task);
    }

    // One OS thread runs ALL tasks — no Thread() anywhere
    public void run() {
        while (!ready.isEmpty()) {
            CoroTask task = ready.poll();
            boolean hasMore = task.step(); // run ONE step of the task
            if (hasMore) {
                ready.add(task); // put it back to run again later
            }
        }
    }

    public static void main(String[] args) {
        CoopScheduler scheduler = new CoopScheduler();

        // Task A: counts to 3
        int[] aCount = {0};
        scheduler.submit(() -> {
            System.out.println("Task A step " + aCount[0]);
            return ++aCount[0] < 3;
        });

        // Task B: counts to 3
        int[] bCount = {0};
        scheduler.submit(() -> {
            System.out.println("Task B step " + bCount[0]);
            return ++bCount[0] < 3;
        });

        scheduler.run();
    }
}
