import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment parent;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, UserFunction> functions = new HashMap<>();

    private boolean returnFlag = false;
    private Object returnValue = null;

    public Environment() { this(null); }
    public Environment(Environment parent) { this.parent = parent; }

    public void define(String name, Object value) {
        variables.put(name, value);
    }

    public void assign(String name, Object value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else if (parent != null) {
            parent.assign(name, value);
        } else {
            throw new RuntimeException("Variable not defined: " + name);
        }
    }

    public Object get(String name) {
        if (variables.containsKey(name)) return variables.get(name);
        if (parent != null) return parent.get(name);
        throw new RuntimeException("Variable not defined: " + name);
    }

    public void defineFunction(String name, UserFunction func) {
        functions.put(name, func);
    }

    public UserFunction getFunction(String name) {
        if (functions.containsKey(name)) return functions.get(name);
        if (parent != null) return parent.getFunction(name);
        return null;
    }

    public void setReturnFlag(boolean flag, Object val) {
        this.returnFlag = flag;
        this.returnValue = val;
    }

    public boolean isReturnFlag() {
        return returnFlag;
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
