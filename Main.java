import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("input.txt");
        String code = "";
        try {
            code = Files.readString(path);
        } catch (IOException e) {
            System.err.println("Failed to read input.txt: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

//         Debug: Print all tokens to see what the lexer produces
//         System.out.println("Tokens:");
//         for (int i = 0; i < tokens.size(); i++) {
//             Token token = tokens.get(i);
//             System.out.println(i + ": " + token.type + "(" + token.value + ")");
//         }
//         System.out.println();

        try {
            Parser parser = new Parser(tokens);
            ASTNode program = parser.parse();

            Environment globalEnv = new Environment();
            program.evaluate(globalEnv);
        } catch (RuntimeException e) {
            System.err.println("Parser error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
