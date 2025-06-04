public class Token {
    public enum Type {
        LET, PRINT, IF, ELSE, WHILE, FOR, FUNCTION, RETURN,
        IDENT, NUMBER,
        OP,      // +, -, *, /, ==, !=, <, >, <=, >=, &&, ||, !
        EQ,      // =
        LPAREN, RPAREN,
        LBRACE, RBRACE,
        COMMA,
        SEMICOLON,
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
