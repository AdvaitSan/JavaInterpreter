import java.util.*;
import java.util.regex.*;

public class Lexer {
    private final String input;
    private int pos = 0;

    private static final Map<String, Token.Type> keywords = Map.of(
            "let", Token.Type.LET,
            "print", Token.Type.PRINT,
            "if", Token.Type.IF,
            "else", Token.Type.ELSE,
            "while", Token.Type.WHILE,
            "for", Token.Type.FOR,
            "function", Token.Type.FUNCTION,
            "return", Token.Type.RETURN
    );

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (Character.isDigit(c)) {
                tokens.add(number());
                continue;
            }

            if (Character.isLetter(c) || c == '_') {
                tokens.add(identifier());
                continue;
            }

            switch (c) {
                case '+': case '-': case '*': case '/':
                case '!':
                case '<': case '>':
                case '&': case '|':
                    tokens.add(operator());
                    continue;
                case '=':
                    if (peekNext() == '=') {
                        pos += 2;
                        tokens.add(new Token(Token.Type.OP, "=="));
                    } else {
                        pos++;
                        tokens.add(new Token(Token.Type.EQ, "="));
                    }
                    continue;
                case '(':
                    pos++;
                    tokens.add(new Token(Token.Type.LPAREN, "("));
                    continue;
                case ')':
                    pos++;
                    tokens.add(new Token(Token.Type.RPAREN, ")"));
                    continue;
                case '{':
                    pos++;
                    tokens.add(new Token(Token.Type.LBRACE, "{"));
                    continue;
                case '}':
                    pos++;
                    tokens.add(new Token(Token.Type.RBRACE, "}"));
                    continue;
                case ',':
                    pos++;
                    tokens.add(new Token(Token.Type.COMMA, ","));
                    continue;
                case ';':
                    pos++;
                    tokens.add(new Token(Token.Type.SEMICOLON, ";"));
                    continue;
                default:
                    throw new RuntimeException("Unexpected char: " + c);
            }
        }
        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }

    private char peekNext() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private Token number() {
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
        String val = input.substring(start, pos);
        return new Token(Token.Type.NUMBER, val);
    }

    private Token identifier() {
        int start = pos;
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            pos++;
        }
        String word = input.substring(start, pos);
        Token.Type type = keywords.getOrDefault(word, Token.Type.IDENT);
        return new Token(type, word);
    }

    private Token operator() {
        // Support multi-char operators like ==, !=, <=, >=, &&, ||
        if (pos + 1 < input.length()) {
            String two = input.substring(pos, pos + 2);
            if (two.equals("==") || two.equals("!=") || two.equals("<=") || two.equals(">=") ||
                    two.equals("&&") || two.equals("||")) {
                pos += 2;
                return new Token(Token.Type.OP, two);
            }
        }
        char one = input.charAt(pos++);
        return new Token(Token.Type.OP, String.valueOf(one));
    }
}
