import java.util.*;

// === Stack Frame ===
// Represents one function call in the call stack.
// Stores the function name and the argument values passed to it.
class StackFrame {
    public final String functionName;
    public final List<Object> argValues;

    public StackFrame(String functionName, List<Object> argValues) {
        this.functionName = functionName;
        this.argValues = argValues;
    }

    @Override
    public String toString() {
        // Format: "at factorial(10)"
        StringBuilder sb = new StringBuilder("at ");
        sb.append(functionName).append("(");
        for (int i = 0; i < argValues.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(argValues.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}

// === Call Stack ===
// Tracks the chain of active function calls.
// Push a frame when entering a function, pop it when leaving.
// Enforces a max depth to catch infinite recursion early.
class CallStack {
    private static final int MAX_DEPTH = 500;

    private final Stack<StackFrame> frames = new Stack<>();

    // Push a new frame onto the stack.
    // Throws if recursion depth is exceeded.
    public void push(String functionName, List<Object> argValues) {
        if (frames.size() >= MAX_DEPTH) {
            throw new RuntimeException(
                "Stack overflow: max recursion depth of " + MAX_DEPTH + " exceeded.\n" + formatTrace()
            );
        }
        frames.push(new StackFrame(functionName, argValues));
    }

    // Pop the top frame off the stack (called on function return).
    public void pop() {
        if (!frames.isEmpty()) {
            frames.pop();
        }
    }

    // How many frames are currently on the stack.
    public int depth() {
        return frames.size();
    }

    // Format all frames as a readable trace (most recent call first).
    public String formatTrace() {
        if (frames.isEmpty()) return "  (empty call stack)";
        StringBuilder sb = new StringBuilder();
        // Iterate in reverse so top of stack (most recent) comes first
        List<StackFrame> list = new ArrayList<>(frames);
        for (int i = list.size() - 1; i >= 0; i--) {
            sb.append("  ").append(list.get(i)).append("\n");
        }
        return sb.toString();
    }

    // Print the current stack trace to stderr.
    public void printStackTrace() {
        System.err.println("Call stack (most recent call first):");
        System.err.print(formatTrace());
    }
}
