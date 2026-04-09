import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment parent;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, UserFunction> functions = new HashMap<>();
    private final Map<String, BuiltinFunction> builtins = new HashMap<>();

    private boolean returnFlag = false;
    private Object returnValue = null;

    // Shared call stack — all environments in the same program share one stack.
    private final CallStack callStack;

    // === Root environment constructor ===
    // Creates a fresh call stack for a new program run.
    public Environment() {
        this.parent = null;
        this.callStack = new CallStack();
    }

    // === Nested environment constructor (for scopes) ===
    // Inherits the parent's call stack so the whole program shares one.
    public Environment(Environment parent) {
        this.parent = parent;
        this.callStack = parent.callStack;
    }

    // === Get the shared call stack ===
    public CallStack getCallStack() {
        return callStack;
    }

    /**
     * Define a new variable in the current environment scope.
     * @param name Variable name
     * @param value Variable value
     */
    public void define(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Assign a value to an existing variable. Searches up the scope chain.
     * Throws if the variable is not defined.
     */
    public void assign(String name, Object value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else if (parent != null) {
            parent.assign(name, value);
        } else {
            throw new RuntimeException("Variable not defined: " + name);
        }
    }

    /**
     * Get the value of a variable by name. Searches up the scope chain.
     * Throws if the variable is not defined.
     */
    public Object get(String name) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Variable name is null or empty in Environment.get()");
        }
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeException("Variable not defined: " + name);
    }

    /**
     * Check if variable is defined in current scope or any parent.
     */
    public boolean hasVariable(String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        return parent != null && parent.hasVariable(name);
    }

    /**
     * Define a user-defined function in the current environment scope.
     */
    public void defineFunction(String name, UserFunction func) {
        functions.put(name, func);
    }

    /**
     * Get a user-defined function by name. Searches up the scope chain.
     * Returns null if not found.
     */
    public UserFunction getFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        if (parent != null) {
            return parent.getFunction(name);
        }
        return null;
    }

    /**
     * Register a built-in function in this environment.
     */
    public void defineBuiltin(String name, BuiltinFunction func) {
        builtins.put(name, func);
    }

    /**
     * Get a built-in function by name. Searches up the scope chain.
     * Returns null if not found.
     */
    public BuiltinFunction getBuiltin(String name) {
        if (builtins.containsKey(name)) {
            return builtins.get(name);
        }
        if (parent != null) {
            return parent.getBuiltin(name);
        }
        return null;
    }

    /**
     * Set the return flag and value used to indicate a return from function execution.
     */
    public void setReturnFlag(boolean flag, Object val) {
        this.returnFlag = flag;
        this.returnValue = val;
    }

    /**
     * Check if the return flag is set.
     */
    public boolean isReturnFlag() {
        return returnFlag;
    }

    /**
     * Get the return value associated with the return flag.
     */
    public Object getReturnValue() {
        return returnValue;
    }
}
