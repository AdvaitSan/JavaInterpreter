public class Interpreter {
    public void execute(ASTNode program) {
        Environment env = new Environment();
        program.evaluate(env);
    }
}
