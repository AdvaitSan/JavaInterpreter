import java.util.Stack;

public class EvalTask implements CoroTask {
    public final Stack<EvalStep> instructions = new Stack<>();
    public final Stack<Object> operands = new Stack<>();
    
    public boolean isYielding = false;
    public boolean isDone = false;
    
    public EvalTask(ASTNode rootNode, Environment env) {
        rootNode.pushEval(this, env);
    }
    
    public void push(EvalStep step) {
        instructions.push(step);
    }
    
    @Override
    public boolean step() {
        if (instructions.isEmpty() || isDone) {
            return false;
        }
        
        isYielding = false; 
        
        EvalStep current = instructions.pop();
        current.execute(this);
        
        return !instructions.isEmpty() && !isDone;
    }
}
