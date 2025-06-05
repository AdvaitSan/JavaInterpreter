import java.util.*;

public class Lexer {
    private final String input;
    private int pos = 0;
    private int lineStartPos = 0;  // Position of line start (for indentation)
    private final Stack<Integer> indentStack = new Stack<>();

    // Keywords mapping
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
        this.input = input.replace("\r\n", "\n"); // normalize newlines
        indentStack.push(0);  // initial indent level 0
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char c = input.charAt(pos);

            // Handle start of line for indentation
            if (pos == lineStartPos) {
                int indent = countIndentation();
                int prevIndent = indentStack.peek();

                if (indent > prevIndent) {
                    indentStack.push(indent);
                    tokens.add(new Token(Token.Type.INDENT, ""));
                } else {
                    while (indent < prevIndent) {
                        indentStack.pop();
                        prevIndent = indentStack.peek();
                        tokens.add(new Token(Token.Type.DEDENT, ""));
                    }
                    if (indent != prevIndent) {
                        throw new RuntimeException("Indentation error at position " + pos);
                    }
                }
            }

            c = input.charAt(pos);

            if (c == '#') {
                skipComment();
                continue;
            }

            if (c == '\n') {
                pos++;
                lineStartPos = pos;
                tokens.add(new Token(Token.Type.NEWLINE, "\n"));
                continue;
            }

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (c == '"' || c == '\'') {
                tokens.add(string());
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
                case ':':
                    pos++;
                    tokens.add(new Token(Token.Type.COLON, ":"));
                    continue;
                default:
                    throw new RuntimeException("Unexpected char: " + c + " at pos " + pos);
            }
        }

        // On EOF, unwind remaining indentations
        while (indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(Token.Type.DEDENT, ""));
        }

        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }

    private int countIndentation() {
        int count = 0;
        int i = lineStartPos;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == ' ') count++;
            else if (c == '\t') count += 4; // treat tab as 4 spaces (adjust as needed)
            else break;
            i++;
        }
        pos = i;  // advance pos past indentation
        return count;
    }

    private void skipComment() {
        while (pos < input.length() && input.charAt(pos) != '\n') {
            pos++;
        }
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
        // Check for decimal part
        if (pos < input.length() && input.charAt(pos) == '.') {
            pos++;
            if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
                throw new RuntimeException("Invalid float literal at pos " + pos);
            }
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                pos++;
            }
        }
        String val = input.substring(start, pos);
        return new Token(Token.Type.NUMBER, val);
    }

    private Token string() {
        char quote = input.charAt(pos);
        pos++; // skip opening quote
        int start = pos;
        while (pos < input.length() && input.charAt(pos) != quote) {
            // TODO: Handle escape sequences if needed
            pos++;
        }
        if (pos >= input.length()) {
            throw new RuntimeException("Unterminated string literal starting at " + start);
        }
        String val = input.substring(start, pos);
        pos++; // skip closing quote
        return new Token(Token.Type.STRING, val);
    }

    private Token identifier() {
        int start = pos;
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            pos++;
        }
        String word = input.substring(start, pos);
        Token.Type type = keywords.getOrDefault(word, Token.Type.INDENT);
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
