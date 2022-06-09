package plc.project;

import java.util.List;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have its own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;


    private int parseIndex(boolean present) {
        if (present) {
            return tokens.get(0).getIndex();
        }
        else {
            return tokens.get(-1).getLiteral().length() + tokens.get(-1).getIndex();
        }
    }


    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        List<Ast.Method> methods = new ArrayList<Ast.Method>();
        List<Ast.Field> fields = new ArrayList<Ast.Field>();

        if (peek(Token.Type.IDENTIFIER) && tokens.has(0)) {
            while (peek(Token.Type.IDENTIFIER)) {
                if (peek("LET")) {
                    while (peek("LET")) {
                        fields.add(parseField());
                        if ((!peek("LET") && !peek("DEF")) && tokens.has(0)) {
                            throw new ParseException("Not Valid Let or Def" + " At Index:" + parseIndex(true), tokens.get(0).getIndex());
                        }
                    }
                }
                if (peek("DEF")) {
                    while (peek("DEF")) {
                        methods.add(parseMethod());
                        if (!peek("DEF") && tokens.has(0)) {
                            throw new ParseException("Not Valid Def" + " At Index:" + parseIndex(true), tokens.get(0).getIndex());
                        }
                    }
                }
            }
        }
        if (!tokens.has(0))
            return new Ast.Source(fields,methods);
        else
            throw new ParseException("Not Valid ID" + " At Index:" + tokens.get(0).getIndex(), tokens.get(0).getIndex());
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (peek("LET")) {
            return parseDeclarationStatement();
        } else if (peek("IF")) {
            return parseIfStatement();
        } else if (peek("FOR")) {
            return parseForStatement();
        } else if (peek("WHILE")) {
            return parseWhileStatement();
        } else if (peek("RETURN")) {
            return parseReturnStatement();
        } else {
            Ast.Expr temp = parseExpression();
            if (peek("=")) {
                match("=");
                Ast.Expr val = parseExpression();
                if (peek(";")) {
                    match(";");
                    return new Ast.Stmt.Assignment(temp, val);
                } else {
                    if (tokens.has(0)) {
                        throw new ParseException("Not Valid ;" + " At Index:" + tokens.get(0).getIndex(), tokens.get(0).getIndex());
                    } else {
                        throw new ParseException("Not Valid ;" + " At Index:" + (parseIndex(false)), parseIndex(false));
                    }
                }
            } else {
                if (peek(";")) {
                    match(";");
                    return new Ast.Stmt.Expression(temp);
                } else {
                    if (tokens.has(0))
                        throw new ParseException("Not Valid ;" + " At Index:" + tokens.get(0).getIndex(),
                                tokens.get(0).getIndex());
                    else {
                        throw new ParseException("Not Valid ;" + " At Index:" + (parseIndex(false)), parseIndex(false));
                    }
                }
            }
        }
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
        /*FIXME: This may or may not be correct, but it is not due til project 2B, so I will leave it here for now.
        Ast.Stmt.Return ret = new Ast.Stmt.Return(parseExpression());
        return ret;*/
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        Ast.Expr expr = parseEqualityExpression();
        while (match("AND")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseEqualityExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match("OR")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseEqualityExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        return expr;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        Ast.Expr expr = parseAdditiveExpression();
        while (match("<")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseAdditiveExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match("<=")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseAdditiveExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match(">")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseAdditiveExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match(">=")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseAdditiveExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match("==")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseAdditiveExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match("!=")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseAdditiveExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        return expr;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        Ast.Expr expr = parseMultiplicativeExpression();
        while (match("+")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseMultiplicativeExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match("-")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseMultiplicativeExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        return expr;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        Ast.Expr expr = parseSecondaryExpression();
        while (match("*")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseSecondaryExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        while (match("/")) {
            String operator = tokens.get(-1).getLiteral();
            Ast.Expr right = parseSecondaryExpression();
            expr = new Ast.Expr.Binary(operator, expr, right);
        }
        return expr;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        Ast.Expr expr = parsePrimaryExpression();
        List<Ast.Expr> list = new ArrayList<Ast.Expr>();
        while (match(".")) {
            if (!match(Token.Type.IDENTIFIER))
                throw new ParseException("Token.Type.IDENTIFIER should follow '.'", -1);
            else{
                String functionName = tokens.get(-1).getLiteral();
                if (match("(")) {
                        expr = new Ast.Expr.Function(Optional.of(expr), functionName, list);
                    //FIXME: Finish this part of function; might first need to fix line 341 (add case for multiple vals
                    // separated by commas)
                        /*while (match(",")) {
                            list.add(expr);
                            expr = new Ast.Expr.Function(Optional.of(expr), functionName, list);
                        }*/
                    if (!match(")")) {
                        //FIXME: replace -1 in next line with true index
                        throw new ParseException("Expected closed parenthesis in parseSecondaryExpression", -1);
                    }
                } else {
                    expr = new Ast.Expr.Access(Optional.of(expr), functionName);
                }
            }
        }
                return expr;
        }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        if (match("NIL")) {
            return new Ast.Expr.Literal(null);
        } else if (match("TRUE")) {
            return new Ast.Expr.Literal(true);
        } else if (match("FALSE")) {
            return new Ast.Expr.Literal(false);
        } else if (match(Token.Type.INTEGER)) {
            BigInteger num = new BigInteger(tokens.get(-1).getLiteral());
            return new Ast.Expr.Literal(num);
        }
        else if (match(Token.Type.DECIMAL)) {
            BigDecimal num = new BigDecimal(tokens.get(-1).getLiteral());
            return new Ast.Expr.Literal(num);
        }
        else if (match(Token.Type.IDENTIFIER)) {
            String name = tokens.get(-1).getLiteral();
            if (match("(")) {
                //FIXME: add case for multiple expressions separated by commas
                Ast.Expr expr = parseExpression();
                if (!match(")")) {
                    //FIXME: replace -1 in next line with true index
                    throw new ParseException("Expected closed parenthesis in parsePrimaryExpression", -1);
                }
                return new Ast.Expr.Group(expr);
            }
            return new Ast.Expr.Access(Optional.empty(), name);
        }
        else if (match(Token.Type.CHARACTER)) {
            Character character = tokens.get(-1).getLiteral().charAt(1);
            return new Ast.Expr.Literal(character);
        }
        else if (match(Token.Type.STRING)) {
            String string = tokens.get(-1).getLiteral();
            string = string.substring(1,string.length()-1);
            //FIXME: Modify this method to handle escape characters (see test)
            return new Ast.Expr.Literal(string);
        }

        else if (match("(")) {
            Ast.Expr expr = parseExpression();
            if (!match(")")) {
                //FIXME: replace -1 in next line with true index
                throw new ParseException("Expected closed parenthesis", -1);
            }
            return new Ast.Expr.Group(expr);
        }

        else {
            //FIXME: replace -1 in next line with true index
            throw new ParseException("Invalid primary expression", -1);
            //Phillip Note:  The index can be found with token.getIndex(); is this the token index (wrong!) or
            // char index (correct!)?  If it is the wrong one, maybe we can use a similar char stream method from P1.
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            }
            else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            }
            else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            }
            else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++)
                tokens.advance();
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
