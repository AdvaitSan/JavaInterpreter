import java.util.List;

// === Interpreter ===
// The central executor. Creates the environment, registers built-ins,
// runs the program, and prints a call stack trace on errors.
public class Interpreter {

    public void execute(ASTNode program) {
        // Root environment — this also creates the shared CallStack
        Environment env = new Environment();

        // Register all built-in functions (len, str, abs, etc.)
        BuiltinsRegistry.register(env);

        try {
            program.evaluate(env);
        } catch (RuntimeException e) {
            // Print the error message (which now includes the call stack trace)
            System.err.println("Runtime error: " + e.getMessage());
        }
    }
}
