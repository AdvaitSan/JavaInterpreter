import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token current() {
        if (pos >= tokens.size()) return new Token(Token.Type.EOF, "");
        return tokens.get(pos);
    }

    private void skipNewlines() {
        while (current().type == Token.Type.NEWLINE) {
            pos++;
        }
    }

    private void skipEmptyIndents() {
        while (current().type == Token.Type.INDENT && current().value.isEmpty()) {
            pos++;
        }
    }

    private Token consume(Token.Type type) {
        skipEmptyIndents();
        skipNewlines();
        if (current().type == type) {
            return tokens.get(pos++);
        }
        throw new RuntimeException("Expected token " + type + " but found " + current());
    }

    private boolean match(Token.Type type) {
        skipEmptyIndents();
        skipNewlines();
        if (current().type == type) {
            pos++;
            return true;
        }
        return false;
    }

    public ASTNode parse() {
        List<ASTNode> statements = new ArrayList<>();
        skipEmptyIndents();
        skipNewlines();

        while (current().type != Token.Type.EOF) {
            if (current().type == Token.Type.EOF) break;
            statements.add(statement());
            skipNewlines();
        }
        return new BlockNode(statements);
    }

    private ASTNode statement() {
        skipEmptyIndents();
        Token tok = current();
        switch (tok.type) {
            case LET:
                return letStatement();
            case PRINT:
                return printStatement();
            case IF:
                return ifStatement();
            case WHILE:
                return whileStatement();
            case FOR:
                return forStatement();
            case FUNCTION:
                return functionStatement();
            case RETURN:
                return returnStatement();
            default:
                if (tok.type == Token.Type.INDENT && !tok.value.isEmpty() && lookAhead(1).type == Token.Type.EQ) {
                    return assignStatement();
                } else {
                    // Expression statement - consume semicolon here
                    ASTNode expr = expression();
                    consumeEndOfStatement();
                    return expr;
                }
        }
    }

    private void consumeEndOfStatement() {
        // In Python-style syntax, statements can end with newline or semicolon
        if (current().type == Token.Type.SEMICOLON) {
            pos++;
        }
        // Newlines will be skipped automatically by skipNewlines()
    }

    private ASTNode letStatement() {
        consume(Token.Type.LET);
        String name = consume(Token.Type.INDENT).value;
        consume(Token.Type.EQ);
        ASTNode expr = expression();
        consumeEndOfStatement();
        return new LetNode(name, expr);
    }

    private ASTNode assignStatement() {
        String name = consume(Token.Type.INDENT).value;
        consume(Token.Type.EQ);
        ASTNode expr = expression();
        consumeEndOfStatement();
        return new AssignNode(name, expr);
    }

    private ASTNode printStatement() {
        consume(Token.Type.PRINT);
        consume(Token.Type.LPAREN);
        ASTNode expr = expression();
        consume(Token.Type.RPAREN);
        consumeEndOfStatement();
        return new PrintNode(expr);
    }

    private ASTNode ifStatement() {
        consume(Token.Type.IF);
        consume(Token.Type.LPAREN);
        ASTNode cond = expression();
        consume(Token.Type.RPAREN);
        consume(Token.Type.COLON); // Expect colon after condition
        ASTNode thenBlock = indentedBlock();
        ASTNode elseBlock = null;

        skipNewlines();
        if (current().type == Token.Type.ELSE) {
            consume(Token.Type.ELSE);
            consume(Token.Type.COLON);
            elseBlock = indentedBlock();
        }
        return new IfNode(cond, thenBlock, elseBlock);
    }

    private ASTNode whileStatement() {
        consume(Token.Type.WHILE);
        consume(Token.Type.LPAREN);
        ASTNode cond = expression();
        consume(Token.Type.RPAREN);
        consume(Token.Type.COLON); // Expect colon after condition
        ASTNode body = indentedBlock();
        return new WhileNode(cond, body);
    }

    private ASTNode forStatement() {
        consume(Token.Type.FOR);
        consume(Token.Type.LPAREN);

        ASTNode init = null;
        if (current().type != Token.Type.SEMICOLON) {
            if (current().type == Token.Type.LET) {
                init = letStatementWithoutEndConsume();
            } else if (current().type == Token.Type.INDENT && !current().value.isEmpty() && lookAhead(1).type == Token.Type.EQ) {
                init = assignStatementWithoutEndConsume();
            } else {
                init = expression();
            }
        }
        consume(Token.Type.SEMICOLON);

        ASTNode condition = null;
        if (current().type != Token.Type.SEMICOLON) {
            condition = expression();
        }
        consume(Token.Type.SEMICOLON);

        ASTNode update = null;
        if (current().type != Token.Type.RPAREN) {
            update = expression();
        }
        consume(Token.Type.RPAREN);
        consume(Token.Type.COLON); // Expect colon after for header

        ASTNode body = indentedBlock();
        return new ForNode(init, condition, update, body);
    }

    private ASTNode letStatementWithoutEndConsume() {
        consume(Token.Type.LET);
        String name = consume(Token.Type.INDENT).value;
        consume(Token.Type.EQ);
        ASTNode expr = expression();
        return new LetNode(name, expr);
    }

    private ASTNode assignStatementWithoutEndConsume() {
        String name = consume(Token.Type.INDENT).value;
        consume(Token.Type.EQ);
        ASTNode expr = expression();
        return new AssignNode(name, expr);
    }

    private ASTNode functionStatement() {
        consume(Token.Type.FUNCTION);
        String name = consume(Token.Type.INDENT).value;
        consume(Token.Type.LPAREN);
        List<String> params = new ArrayList<>();
        if (current().type != Token.Type.RPAREN) {
            params.add(consume(Token.Type.INDENT).value);
            while (match(Token.Type.COMMA)) {
                params.add(consume(Token.Type.INDENT).value);
            }
        }
        consume(Token.Type.RPAREN);
        consume(Token.Type.COLON); // Expect colon after function header
        ASTNode body = indentedBlock();
        return new FunctionNode(name, params, body);
    }

    private ASTNode returnStatement() {
        consume(Token.Type.RETURN);
        ASTNode expr = expression();
        consumeEndOfStatement();
        return new ReturnNode(expr);
    }

    // New method for handling indented blocks (Python-style)
    private ASTNode indentedBlock() {
        skipNewlines();

        // Expect an INDENT token to start the block
        if (current().type != Token.Type.INDENT) {
            throw new RuntimeException("Expected indented block");
        }

        List<ASTNode> stmts = new ArrayList<>();

        // Keep parsing statements until we hit a DEDENT or EOF
        while (current().type != Token.Type.DEDENT && current().type != Token.Type.EOF) {
            // Skip any INDENT tokens that represent the indentation level
            if (current().type == Token.Type.INDENT && current().value.isEmpty()) {
                pos++;
                continue;
            }

            stmts.add(statement());
            skipNewlines();
        }

        // Consume the DEDENT token if present
        if (current().type == Token.Type.DEDENT) {
            pos++;
        }

        return new BlockNode(stmts);
    }

    // Keep the old block method for backward compatibility with braces
    private ASTNode block() {
        consume(Token.Type.LBRACE);
        List<ASTNode> stmts = new ArrayList<>();
        while (current().type != Token.Type.RBRACE) {
            stmts.add(statement());
        }
        consume(Token.Type.RBRACE);
        return new BlockNode(stmts);
    }

    // --- Expression Parsing (unchanged) ---

    private ASTNode expression() {
        return assignment();
    }

    private ASTNode assignment() {
        ASTNode left = logicalOr();

        if (current().type == Token.Type.EQ) {
            consume(Token.Type.EQ);
            ASTNode right = assignment();
            if (left instanceof VariableNode) {
                String varName = ((VariableNode) left).name;
                return new AssignNode(varName, right);
            } else {
                throw new RuntimeException("Invalid assignment target");
            }
        }

        return left;
    }

    private ASTNode logicalOr() {
        ASTNode left = logicalAnd();
        while (matchOperator("||")) {
            String op = "||";
            ASTNode right = logicalAnd();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    private ASTNode logicalAnd() {
        ASTNode left = equality();
        while (matchOperator("&&")) {
            String op = "&&";
            ASTNode right = equality();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    private ASTNode equality() {
        ASTNode left = relational();
        while (matchOperator("==") || matchOperator("!=")) {
            String op = tokens.get(pos - 1).value;
            ASTNode right = relational();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    private ASTNode relational() {
        ASTNode left = additive();
        while (matchOperator("<") || matchOperator("<=") || matchOperator(">") || matchOperator(">=")) {
            String op = tokens.get(pos - 1).value;
            ASTNode right = additive();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    private ASTNode additive() {
        ASTNode left = multiplicative();
        while (matchOperator("+") || matchOperator("-")) {
            String op = tokens.get(pos - 1).value;
            ASTNode right = multiplicative();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    private ASTNode multiplicative() {
        ASTNode left = unary();
        while (matchOperator("*") || matchOperator("/")) {
            String op = tokens.get(pos - 1).value;
            ASTNode right = unary();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    private ASTNode unary() {
        if (matchOperator("!") || matchOperator("-")) {
            String op = tokens.get(pos - 1).value;
            ASTNode expr = unary();
            return new UnaryOpNode(op, expr);
        }
        return primary();
    }

    private ASTNode primary() {
        skipEmptyIndents();
        Token tok = current();
        switch (tok.type) {
            case NUMBER:
                pos++;
                return new NumberNode(Integer.parseInt(tok.value));
            case STRING:
                pos++;
                return new StringNode(tok.value);
            case INDENT:
                if (!tok.value.isEmpty()) { // Only process non-empty INDENT tokens as identifiers
                    pos++;
                    if (match(Token.Type.LPAREN)) {
                        List<ASTNode> args = new ArrayList<>();
                        if (current().type != Token.Type.RPAREN) {
                            args.add(expression());
                            while (match(Token.Type.COMMA)) {
                                args.add(expression());
                            }
                        }
                        consume(Token.Type.RPAREN);
                        return new FunctionCallNode(tok.value, args);
                    }
                    return new VariableNode(tok.value);
                } else {
                    throw new RuntimeException("Unexpected empty indent token");
                }
            case LPAREN:
                consume(Token.Type.LPAREN);
                ASTNode expr = expression();
                consume(Token.Type.RPAREN);
                return expr;
            default:
                throw new RuntimeException("Unexpected token " + tok);
        }
    }

    private boolean matchOperator(String op) {
        skipEmptyIndents();
        if (current().type == Token.Type.OP && current().value.equals(op)) {
            pos++;
            return true;
        }
        return false;
    }

    private Token lookAhead(int offset) {
        if (pos + offset >= tokens.size()) return new Token(Token.Type.EOF, "");
        return tokens.get(pos + offset);
    }
}