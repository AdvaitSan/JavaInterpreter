// A "task" that knows how to pause and resume itself
public interface CoroTask {
    boolean step(); // returns true = more work to do, false = done
}
