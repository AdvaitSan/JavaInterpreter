import java.util.*;

// === AST Node Base ===
abstract class ASTNode {
    public abstract void pushEval(EvalTask task, Environment env);
}

// === Block Node ===
class BlockNode extends ASTNode {
    public final List<ASTNode> statements;
    public BlockNode(List<ASTNode> statements) { this.statements = statements; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(new EvalStep() {
            int index = 0;
            public void execute(EvalTask t) {
                if (env.isReturnFlag()) {
                    return; 
                }
                
                if (index > 0) {
                    if (index < statements.size()) {
                        t.operands.pop(); // discard previous stmt result
                    }
                } else if (statements.isEmpty()) {
                    t.operands.push(null);
                    return;
                }

                if (index >= statements.size()) {
                    return; // done
                }

                ASTNode stmt = statements.get(index++);
                t.push(this);
                stmt.pushEval(t, env);
            }
        });
    }
}

// === Variable Declaration (let) ===
class LetNode extends ASTNode {
    public final String name;
    public final ASTNode expr;
    public LetNode(String name, ASTNode expr) { this.name = name; this.expr = expr; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object val = t.operands.peek(); // keep result on stack
            env.define(name, val);
        });
        expr.pushEval(task, env);
    }
}

// === Print Statement ===
class PrintNode extends ASTNode {
    public final ASTNode expr;
    public PrintNode(ASTNode expr) { this.expr = expr; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object val = t.operands.peek();
            System.out.println(val);
        });
        expr.pushEval(task, env);
    }
}

// === If Statement ===
class IfNode extends ASTNode {
    public final ASTNode condition, thenBlock, elseBlock;
    public IfNode(ASTNode condition, ASTNode thenBlock, ASTNode elseBlock) {
        this.condition = condition; this.thenBlock = thenBlock; this.elseBlock = elseBlock;
    }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object cond = t.operands.pop();
            if (isTruthy(cond)) {
                thenBlock.pushEval(t, env);
            } else if (elseBlock != null) {
                elseBlock.pushEval(t, env);
            } else {
                t.operands.push(null);
            }
        });
        condition.pushEval(task, env);
    }
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

// === While Statement ===
class WhileNode extends ASTNode {
    public final ASTNode condition, body;
    public WhileNode(ASTNode condition, ASTNode body) {
        this.condition = condition; this.body = body;
    }
    public void pushEval(EvalTask task, Environment env) {
        task.push(new EvalStep() {
            int state = 0; 
            public void execute(EvalTask t) {
                if (state == 0) {
                    state = 1;
                    t.push(this);
                    condition.pushEval(t, env);
                } else if (state == 1) {
                    Object cond = t.operands.pop();
                    if (!isTruthy(cond)) {
                        t.operands.push(null); 
                        return; 
                    }
                    state = 2; 
                    t.push(this);
                    body.pushEval(t, env);
                } else if (state == 2) {
                    t.operands.pop(); // discard body result
                    if (env.isReturnFlag()) {
                        t.operands.push(null);
                        return; // return breaks while loop
                    }
                    state = 0;
                    t.push(this); // loop again
                }
            }
        });
    }
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

// === For Statement ===
class ForNode extends ASTNode {
    public final ASTNode init, condition, update, body;
    public ForNode(ASTNode init, ASTNode condition, ASTNode update, ASTNode body) {
        this.init = init; this.condition = condition; this.update = update; this.body = body;
    }
    public void pushEval(EvalTask task, Environment env) {
        Environment loopEnv = new Environment(env);  
        
        task.push(new EvalStep() {
            int state = 0; 
            public void execute(EvalTask t) {
                if (state == 0) {
                    state = 1;
                    t.push(this);
                    if (condition != null) condition.pushEval(t, loopEnv);
                    else t.operands.push(true);
                } else if (state == 1) {
                    Object cond = t.operands.pop();
                    if (!isTruthy(cond)) { t.operands.push(null); return; }
                    state = 2;
                    t.push(this);
                    body.pushEval(t, loopEnv);
                } else if (state == 2) {
                    t.operands.pop(); // discard body result
                    if (loopEnv.isReturnFlag()) { t.operands.push(null); return; }
                    state = 3;
                    t.push(this);
                    if (update != null) update.pushEval(t, loopEnv);
                    else t.operands.push(null);
                } else if (state == 3) {
                    t.operands.pop(); // discard update result
                    state = 0; // restart
                    t.push(this);
                }
            }
        });
        
        task.push(t -> t.operands.pop()); 
        if (init != null) init.pushEval(task, loopEnv);
        else task.operands.push(null);
    }
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        return true;
    }
}

// === Function Definition ===
class FunctionNode extends ASTNode {
    public final String name;
    public final List<String> params;
    public final ASTNode body;
    public FunctionNode(String name, List<String> params, ASTNode body) {
        this.name = name; this.params = params; this.body = body;
    }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            env.defineFunction(name, new UserFunction(params, body, env));
            t.operands.push(null);
        });
    }
}

// === Return Statement ===
class ReturnNode extends ASTNode {
    public final ASTNode expr;
    public ReturnNode(ASTNode expr) { this.expr = expr; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object val = t.operands.peek(); // return value is on stack
            env.setReturnFlag(true, val);
        });
        expr.pushEval(task, env);
    }
}

// === Assignment Statement ===
class AssignNode extends ASTNode {
    public final String name;
    public final ASTNode expr;
    public AssignNode(String name, ASTNode expr) { this.name = name; this.expr = expr; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object val = t.operands.peek(); // keep result on stack
            env.assign(name, val);
        });
        expr.pushEval(task, env);
    }
}

// === Variable Reference ===
class VariableNode extends ASTNode {
    public final String name;
    public VariableNode(String name) { this.name = name; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> t.operands.push(env.get(name)));
    }
}

// === Number Literal ===
class NumberNode extends ASTNode {
    public final int value;
    public NumberNode(int value) { this.value = value; }
    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> t.operands.push(value));
    }
}

// === Binary Operation ===
class BinaryOpNode extends ASTNode {
    public final ASTNode left;
    public final String op;
    public final ASTNode right;

    public BinaryOpNode(ASTNode left, String op, ASTNode right) {
        this.left = left; this.op = op; this.right = right;
    }

    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object rightVal = t.operands.pop();
            Object leftVal = t.operands.pop();

            switch (op) {
                case "+": 
                    if (leftVal instanceof String || rightVal instanceof String) {
                        t.operands.push(String.valueOf(leftVal) + String.valueOf(rightVal));
                        break;
                    }
                    t.operands.push(toNumber(leftVal) + toNumber(rightVal));
                    break;
                case "-": t.operands.push(toNumber(leftVal) - toNumber(rightVal)); break;
                case "*": t.operands.push(toNumber(leftVal) * toNumber(rightVal)); break;
                case "/": t.operands.push(toNumber(leftVal) / toNumber(rightVal)); break;
                case "==": t.operands.push(leftVal.equals(rightVal)); break;
                case "!=": t.operands.push(!leftVal.equals(rightVal)); break;
                case "<": t.operands.push(toNumber(leftVal) < toNumber(rightVal)); break;
                case "<=": t.operands.push(toNumber(leftVal) <= toNumber(rightVal)); break;
                case ">": t.operands.push(toNumber(leftVal) > toNumber(rightVal)); break;
                case ">=": t.operands.push(toNumber(leftVal) >= toNumber(rightVal)); break;
                case "&&": t.operands.push(isTruthy(leftVal) && isTruthy(rightVal)); break;
                case "||": t.operands.push(isTruthy(leftVal) || isTruthy(rightVal)); break;
                default:
                    throw new RuntimeException("Unknown operator " + op);
            }
        });
        right.pushEval(task, env);
        left.pushEval(task, env);
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

// === Unary Operation ===
class UnaryOpNode extends ASTNode {
    public final String op;
    public final ASTNode expr;
    public UnaryOpNode(String op, ASTNode expr) {
        this.op = op; this.expr = expr;
    }

    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> {
            Object val = t.operands.pop();
            switch (op) {
                case "!": t.operands.push(!isTruthy(val)); break;
                case "-": t.operands.push(-toNumber(val)); break;
                default: throw new RuntimeException("Unknown unary operator " + op);
            }
        });
        expr.pushEval(task, env);
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

// === Function Call ===
class FunctionCallNode extends ASTNode {
    public final String name;
    public final List<ASTNode> args;

    public FunctionCallNode(String name, List<ASTNode> args) {
        this.name = name; this.args = args;
    }

    public void pushEval(EvalTask task, Environment env) {
        task.push(new EvalStep() {
            int state = 0; 
            CallStack callStack;
            Environment localEnv;
            
            public void execute(EvalTask t) {
                if (state == 0) {
                    List<Object> argValues = new java.util.ArrayList<>();
                    for (int i=0; i<args.size(); i++) {
                         argValues.add(0, t.operands.pop()); 
                    }
                    
                    callStack = env.getCallStack();
                    callStack.push(name, argValues);
                    
                    try {
                        BuiltinFunction builtin = env.getBuiltin(name);
                        if (builtin != null) {
                            t.operands.push(builtin.call(argValues));
                            callStack.pop();
                            return;
                        }
                        
                        UserFunction func = env.getFunction(name);
                        if (func == null) throw new RuntimeException("Function not found: " + name);
                        if (argValues.size() != func.params.size())
                            throw new RuntimeException("Function " + name + " expects " + func.params.size() + " arguments, got " + argValues.size());
                        
                        localEnv = new Environment(func.env);
                        for(int i=0; i<argValues.size(); i++) localEnv.define(func.params.get(i), argValues.get(i));
                        
                        state = 1;
                        t.push(this);
                        
                        func.body.pushEval(t, localEnv);
                        
                    } catch (RuntimeException e) {
                        if (callStack != null) callStack.pop();
                        if (!e.getMessage().contains("Call stack")) {
                            throw new RuntimeException(e.getMessage() + "\n\nCall stack (most recent call first):\n" + env.getCallStack().formatTrace(), e);
                        }
                        throw e;
                    }
                } else if (state == 1) {
                    if (localEnv.isReturnFlag()) {
                        t.operands.pop(); 
                        t.operands.push(localEnv.getReturnValue());
                        localEnv.setReturnFlag(false, null);
                    }
                    callStack.pop(); 
                }
            }
        });
        
        for(int i = args.size() - 1; i >= 0; i--) {
            args.get(i).pushEval(task, env);
        }
    }
}

// === User Function Holder ===
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

class StringNode extends ASTNode {
    public final String value;

    public StringNode(String value) {
        this.value = value;
    }

    public void pushEval(EvalTask task, Environment env) {
        task.push(t -> t.operands.push(value));
    }
}
