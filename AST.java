import java.util.List;

abstract class ASTNode {
    public abstract Object evaluate(Environment env);
}

class BlockNode extends ASTNode {
    public final List<ASTNode> statements;
    public BlockNode(List<ASTNode> statements) { this.statements = statements; }
    public Object evaluate(Environment env) {
        Object result = null;
        for (ASTNode stmt : statements) {
            result = stmt.evaluate(env);
            if (env.isReturnFlag()) return result;
        }
        return result;
    }
}

class LetNode extends ASTNode {
    public final String name;
    public final ASTNode expr;
    public LetNode(String name, ASTNode expr) { this.name = name; this.expr = expr; }
    public Object evaluate(Environment env) {
        Object value = expr.evaluate(env);
        env.define(name, value);
        return value;
    }
}

class PrintNode extends ASTNode {
    public final ASTNode expr;
    public PrintNode(ASTNode expr) { this.expr = expr; }
    public Object evaluate(Environment env) {
        Object val = expr.evaluate(env);
        System.out.println(val);
        return val;
    }
}

class IfNode extends ASTNode {
    public final ASTNode condition, thenBlock, elseBlock;
    public IfNode(ASTNode condition, ASTNode thenBlock, ASTNode elseBlock) {
        this.condition = condition; this.thenBlock = thenBlock; this.elseBlock = elseBlock;
    }
    public Object evaluate(Environment env) {
        Object cond = condition.evaluate(env);
        if (isTruthy(cond)) {
            return thenBlock.evaluate(env);
        } else if (elseBlock != null) {
            return elseBlock.evaluate(env);
        }
        return null;
    }
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

class WhileNode extends ASTNode {
    public final ASTNode condition, body;
    public WhileNode(ASTNode condition, ASTNode body) {
        this.condition = condition; this.body = body;
    }
    public Object evaluate(Environment env) {
        Object result = null;
        while (true) {
            Object cond = condition.evaluate(env);
            if (!isTruthy(cond)) break;
            result = body.evaluate(env);
            if (env.isReturnFlag()) break;
        }
        return result;
    }
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

class ForNode extends ASTNode {
    public final ASTNode init, condition, update, body;
    public ForNode(ASTNode init, ASTNode condition, ASTNode update, ASTNode body) {
        this.init = init; this.condition = condition; this.update = update; this.body = body;
    }
    public Object evaluate(Environment env) {
        Object result = null;
        Environment loopEnv = new Environment(env);  // new scope for for loop
        if (init != null) init.evaluate(loopEnv);
        while (true) {
            if (condition != null) {
                Object cond = condition.evaluate(loopEnv);
                if (!isTruthy(cond)) break;
            }
            result = body.evaluate(loopEnv);
            if (loopEnv.isReturnFlag()) break;
            if (update != null) update.evaluate(loopEnv);
        }
        return result;
    }
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

class FunctionNode extends ASTNode {
    public final String name;
    public final List<String> params;
    public final ASTNode body;
    public FunctionNode(String name, List<String> params, ASTNode body) {
        this.name = name; this.params = params; this.body = body;
    }
    public Object evaluate(Environment env) {
        env.defineFunction(name, new UserFunction(params, body, env));
        return null;
    }
}

class ReturnNode extends ASTNode {
    public final ASTNode expr;
    public ReturnNode(ASTNode expr) { this.expr = expr; }
    public Object evaluate(Environment env) {
        Object val = expr.evaluate(env);
        env.setReturnFlag(true, val);
        return val;
    }
}

class AssignNode extends ASTNode {
    public final String name;
    public final ASTNode expr;
    public AssignNode(String name, ASTNode expr) { this.name = name; this.expr = expr; }
    public Object evaluate(Environment env) {
        Object value = expr.evaluate(env);
        env.assign(name, value);
        return value;
    }
}

class VariableNode extends ASTNode {
    public final String name;
    public VariableNode(String name) { this.name = name; }
    public Object evaluate(Environment env) {
        return env.get(name);
    }
}

class NumberNode extends ASTNode {
    public final int value;
    public NumberNode(int value) { this.value = value; }
    public Object evaluate(Environment env) { return value; }
}

class BinaryOpNode extends ASTNode {
    public final ASTNode left;
    public final String op;
    public final ASTNode right;

    public BinaryOpNode(ASTNode left, String op, ASTNode right) {
        this.left = left; this.op = op; this.right = right;
    }

    public Object evaluate(Environment env) {
        Object leftVal = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        switch (op) {
            case "+": return toNumber(leftVal) + toNumber(rightVal);
            case "-": return toNumber(leftVal) - toNumber(rightVal);
            case "*": return toNumber(leftVal) * toNumber(rightVal);
            case "/": return toNumber(leftVal) / toNumber(rightVal);
            case "==": return leftVal.equals(rightVal);
            case "!=": return !leftVal.equals(rightVal);
            case "<": return toNumber(leftVal) < toNumber(rightVal);
            case "<=": return toNumber(leftVal) <= toNumber(rightVal);
            case ">": return toNumber(leftVal) > toNumber(rightVal);
            case ">=": return toNumber(leftVal) >= toNumber(rightVal);
            case "&&": return isTruthy(leftVal) && isTruthy(rightVal);
            case "||": return isTruthy(leftVal) || isTruthy(rightVal);
            default:
                throw new RuntimeException("Unknown operator " + op);
        }
    }

    private double toNumber(Object o) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Double) return (Double) o;
        throw new RuntimeException("Expected a number but got " + o);
    }

    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

class UnaryOpNode extends ASTNode {
    public final String op;
    public final ASTNode expr;
    public UnaryOpNode(String op, ASTNode expr) {
        this.op = op; this.expr = expr;
    }

    public Object evaluate(Environment env) {
        Object val = expr.evaluate(env);
        switch (op) {
            case "!": return !isTruthy(val);
            case "-": return -toNumber(val);
            default: throw new RuntimeException("Unknown unary operator " + op);
        }
    }

    private double toNumber(Object o) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Double) return (Double) o;
        throw new RuntimeException("Expected a number but got " + o);
    }

    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

class FunctionCallNode extends ASTNode {
    public final String name;
    public final List<ASTNode> args;

    public FunctionCallNode(String name, List<ASTNode> args) {
        this.name = name; this.args = args;
    }

    public Object evaluate(Environment env) {
        UserFunction func = env.getFunction(name);
        if (func == null) throw new RuntimeException("Function not found: " + name);
        if (args.size() != func.params.size())
            throw new RuntimeException("Function " + name + " expects " + func.params.size() + " arguments, got " + args.size());

        Environment localEnv = new Environment(func.env);
        for (int i = 0; i < args.size(); i++) {
            Object argVal = args.get(i).evaluate(env);
            localEnv.define(func.params.get(i), argVal);
        }
        Object ret = func.body.evaluate(localEnv);
        if (localEnv.isReturnFlag()) {
            localEnv.setReturnFlag(false, null);
            return localEnv.getReturnValue();
        }
        return ret;
    }
}

class UserFunction {
    public final List<String> params;
    public final ASTNode body;
    public final Environment env;

    public UserFunction(List<String> params, ASTNode body, Environment env) {
        this.params = params;
        this.body = body;
        this.env = env;
    }
}
