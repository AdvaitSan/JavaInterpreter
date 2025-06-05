public class Token {
    public enum Type {
        // Keywords
        LET, PRINT, IF, ELSE, WHILE, FOR, FUNCTION, RETURN,

        // Identifiers and literals
        INDENT, NUMBER,DEDENT,NEWLINE,STRING,

        // Operators (arithmetic, comparison, logical, unary)
        OP,      // +, -, *, /, ==, !=, <, >, <=, >=, &&, ||, !

        // Assignment operator
        EQ,      // =

        // Parentheses and braces
        LPAREN, RPAREN,
        LBRACE, RBRACE,

        // Separators
        COMMA,COLON,
        SEMICOLON,

        // End of file/input
        EOF
    }

    public final Type type;
    public final String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}
