import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Read source code from input.txt
        Path path = Paths.get("input.txt");
        String code = "";
        try {
            code = Files.readString(path);
        } catch (IOException e) {
            System.err.println("Failed to read input.txt: " + e.getMessage());
            return;
        }

        // Lex → tokens
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

//         Debug: Print all tokens to see what the lexer produces
//         System.out.println("Tokens:");
//         for (int i = 0; i < tokens.size(); i++) {
//             Token token = tokens.get(i);
//             System.out.println(i + ": " + token.type + "(" + token.value + ")");
//         }
//         System.out.println();

        // Parse → AST
        Parser parser = new Parser(tokens);
        ASTNode program = parser.parse();

        // Interpret — errors are caught and reported inside Interpreter
        Interpreter interpreter = new Interpreter();
        interpreter.execute(program);
    }
}
