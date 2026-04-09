import java.util.List;

// === Built-in Function Interface ===
// All built-in functions implement this interface.
// It's a functional interface so we can use lambdas below.
interface BuiltinFunction {
    Object call(List<Object> args);
}

// === Builtins Registry ===
// Registers all built-in functions into a given environment.
// Call BuiltinsRegistry.register(env) once on the root environment.
class BuiltinsRegistry {

    public static void register(Environment env) {

        // len(s) — length of a string
        env.defineBuiltin("len", args -> {
            checkArgCount("len", args, 1);
            Object arg = args.get(0);
            if (arg instanceof String) return ((String) arg).length();
            throw new RuntimeException("len() expects a string, got: " + arg);
        });

        // str(x) — convert anything to its string representation
        env.defineBuiltin("str", args -> {
            checkArgCount("str", args, 1);
            return String.valueOf(args.get(0));
        });

        // int(x) — convert to integer (truncates doubles, parses strings)
        env.defineBuiltin("int", args -> {
            checkArgCount("int", args, 1);
            Object arg = args.get(0);
            if (arg instanceof Integer) return arg;
            if (arg instanceof Double) return ((Double) arg).intValue();
            if (arg instanceof String) {
                try { return Integer.parseInt((String) arg); }
                catch (NumberFormatException e) {
                    throw new RuntimeException("int() cannot convert: " + arg);
                }
            }
            throw new RuntimeException("int() cannot convert: " + arg);
        });

        // double(x) — convert to floating-point number
        env.defineBuiltin("double", args -> {
            checkArgCount("double", args, 1);
            Object arg = args.get(0);
            if (arg instanceof Double) return arg;
            if (arg instanceof Integer) return ((Integer) arg).doubleValue();
            if (arg instanceof String) {
                try { return Double.parseDouble((String) arg); }
                catch (NumberFormatException e) {
                    throw new RuntimeException("double() cannot convert: " + arg);
                }
            }
            throw new RuntimeException("double() cannot convert: " + arg);
        });

        // abs(x) — absolute value of a number
        env.defineBuiltin("abs", args -> {
            checkArgCount("abs", args, 1);
            Object arg = args.get(0);
            if (arg instanceof Integer) return Math.abs((Integer) arg);
            if (arg instanceof Double) return Math.abs((Double) arg);
            throw new RuntimeException("abs() expects a number, got: " + arg);
        });

        // max(a, b) — larger of two numbers
        env.defineBuiltin("max", args -> {
            checkArgCount("max", args, 2);
            double a = toDouble(args.get(0), "max");
            double b = toDouble(args.get(1), "max");
            double result = Math.max(a, b);
            // Keep integer type if both inputs were integers
            if (args.get(0) instanceof Integer && args.get(1) instanceof Integer)
                return (int) result;
            return result;
        });

        // min(a, b) — smaller of two numbers
        env.defineBuiltin("min", args -> {
            checkArgCount("min", args, 2);
            double a = toDouble(args.get(0), "min");
            double b = toDouble(args.get(1), "min");
            double result = Math.min(a, b);
            if (args.get(0) instanceof Integer && args.get(1) instanceof Integer)
                return (int) result;
            return result;
        });

        // sqrt(x) — square root, always returns a double
        env.defineBuiltin("sqrt", args -> {
            checkArgCount("sqrt", args, 1);
            double x = toDouble(args.get(0), "sqrt");
            return Math.sqrt(x);
        });
    }

    // === Helpers ===

    private static void checkArgCount(String name, List<Object> args, int expected) {
        if (args.size() != expected) {
            throw new RuntimeException(
                name + "() expects " + expected + " argument(s), got " + args.size()
            );
        }
    }

    private static double toDouble(Object o, String funcName) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Double) return (Double) o;
        throw new RuntimeException(funcName + "() expects a number, got: " + o);
    }
}
