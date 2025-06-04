import java.util.List;

public class Main {
    public static void main(String[] args) {
        String code = """
                let j=-2;
                while(j<4){
                j=j+2;
                print(j);
                }
                let n=2;
                if (n == 0) {
                    print(3);
                } else {
                    print(8);
                }
            
            let x = 5;
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        ASTNode program = parser.parse();

        Environment globalEnv = new Environment();
        program.evaluate(globalEnv);
    }
}
