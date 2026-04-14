import java.util.List;
import java.util.ArrayList;

public class Interpreter {
    public void execute(ASTNode program) {
        Environment env = new Environment();
        BuiltinsRegistry.register(env);
        
        CoopScheduler scheduler = new CoopScheduler();

        env.defineBuiltin("spawn", args -> {
            if (args.isEmpty()) throw new RuntimeException("spawn expects at least 1 argument (function name)");
            String funcName = String.valueOf(args.get(0));
            
            List<ASTNode> callArgs = new ArrayList<>();
            for (int i = 1; i < args.size(); i++) {
                final Object val = args.get(i);
                callArgs.add(new ASTNode() {
                    public void pushEval(EvalTask task, Environment loopEnv) {
                        task.push(t -> t.operands.push(val));
                    }
                });
            }
            
            ASTNode callNative = new FunctionCallNode(funcName, callArgs);
            EvalTask newTask = new EvalTask(callNative, env);
            scheduler.submit(newTask);
            return null;
        });

        env.defineBuiltin("yield", args -> {
            return null;
        });

        EvalTask mainTask = new EvalTask(program, env);
        scheduler.submit(mainTask);

        try {
            scheduler.run();
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
        }
    }
}
