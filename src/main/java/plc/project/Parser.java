package plc.project;

import java.sql.Array;
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
                            throw new ParseException("Not Valid Let or Def" + " At Index:" + parseIndex(true), parseIndex(true));
                        }
                    }
                }
                if (peek("DEF")) {
                    while (peek("DEF")) {
                        methods.add(parseMethod());
                        if (!peek("DEF") && tokens.has(0)) {
                            throw new ParseException("Not Valid Def" + " At Index:" + parseIndex(true), parseIndex(true));
                        }
                    }
                }
            }
        }
        if (!tokens.has(0))
            return new Ast.Source(fields,methods);
        else
            throw new ParseException("Not Valid ID" + " At Index:" + parseIndex(true), parseIndex(true));
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        //field ::= 'LET' identifier ('=' expression)? ';'
        match("LET");

        if(!match(Token.Type.IDENTIFIER)) {
            throw new ParseException("Expected Identifier in Field" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        String name = tokens.get(-1).getLiteral();

        Optional<Ast.Expr> value = Optional.empty();

        if(match("=")){
            value = Optional.of(parseExpression());
        }

        if(!match(";")) {
            throw new ParseException("Expected Semicolon in Field" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        Ast.Field field = new Ast.Field(name, value);

        return field;
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        //'DEF' identifier '(' (identifier (',' identifier)*)? ')' 'DO' statement* 'END'

        match("DEF");

        if(!match(Token.Type.IDENTIFIER)) {
            throw new ParseException("Expected Identifier in Method" + " At Index:" + parseIndex(true),
                    parseIndex(true));
        }

        String name = tokens.get(-1).getLiteral();
        List<String> parameters = new ArrayList<>();
        List<Ast.Stmt> statements = new ArrayList<>();

        if(!match("(")) {
            throw new ParseException("Expected Open Parenthesis in Method" + " At Index:" + parseIndex(true),
                    parseIndex(true));
        }

        while(!match(")")) {
            parameters.add(tokens.get(-1).getLiteral());
            if (!peek(")")) {
                while (match(",")) {
                    parameters.add(tokens.get(-1).getLiteral());
                }
            }
        }

        if (!match("DO")) {
            throw new ParseException("Expected \"DO\" in Method" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        while(!peek("END"))
        {
            statements.add(parseStatement());
        }
        match("END");

        return new Ast.Method(name, parameters, statements);
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
                        throw new ParseException("Not Valid ;" + " At Index:" + parseIndex(true), parseIndex(true));
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
                        throw new ParseException("Not Valid ;" + " At Index:" + parseIndex(true), parseIndex(true));

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
        // LET identifier ('=' expression)? ';'

        match("LET");

        if(!match(Token.Type.IDENTIFIER)) {
            throw new ParseException("Expected Identifier in Declaration Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        String name = tokens.get(-1).getLiteral();
        Optional<Ast.Expr> value = Optional.empty();

        if(match("=")) {
            value = Optional.of(parseExpression());
        }

        if(!match(";")) {
            throw new ParseException("Expected Semicolon in Declaration Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        return new Ast.Stmt.Declaration(name, value);
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        //'IF' expression 'DO' statement* ('ELSE' statement*)? 'END'
        match("IF");

        Ast.Expr value = parseExpression();

        if (!match("DO")) {
            throw new ParseException("Expected \"DO\" in If Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        List<Ast.Stmt> thenStatements = new ArrayList<Ast.Stmt>();
        List<Ast.Stmt> elseStatements = new ArrayList<Ast.Stmt>();

        while(!peek("END")) {
            thenStatements.add(parseStatement());
            if (peek("ELSE")) {
                match("ELSE");
                while(!peek("END")){
                    elseStatements.add(parseStatement());
                    if (!tokens.has(0)) {
                        throw new ParseException("Expected \"END\" in If Statement" + " At Index:" + parseIndex(true), parseIndex(true));
                    }
                }
            }
            if (!tokens.has(0)) {
                throw new ParseException("Expected \"END\" in If Statement" + " At Index:" + parseIndex(true), parseIndex(true));
            }
        }

        match("END");
        return new Ast.Stmt.If(value, thenStatements, elseStatements);
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        // 'FOR' identifier 'IN' expression 'DO' statement* 'END'
        match("FOR");

        if (!match(Token.Type.IDENTIFIER)) {
            throw new ParseException("Expected Identifier in For Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        String name = tokens.get(-1).getLiteral();

        if (!match("IN")) {
            throw new ParseException("Expected \"IN\" in For Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        Ast.Expr value = parseExpression();

        if (!match("DO")) {
            throw new ParseException("Expected \"DO\" in For Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        List<Ast.Stmt> stmtArrayList = new ArrayList<>();
        while(!peek("END")) {
            stmtArrayList.add(parseStatement());
        }

        if (!match("END")) {
            throw new ParseException("Expected \"END\" in For Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        return new Ast.Stmt.For(name, value, stmtArrayList);
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        //'WHILE' expression 'DO' statement* 'END'

        match("WHILE");

        Ast.Expr value = parseExpression();

        if (!match("DO")) {
            throw new ParseException("Expected \"DO\" in While Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        List<Ast.Stmt> stmtArrayList = new ArrayList<>();
        while(!peek("END")) {
            stmtArrayList.add(parseStatement());
        }

        if (!match("END")) {
            throw new ParseException("Expected \"END\" in For Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }

        return new Ast.Stmt.While(value, stmtArrayList);
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        //'RETURN' expression ';'
        match("RETURN");

        Ast.Expr value = parseExpression();

        if(!match(";")) {
        throw new ParseException("Expected Semicolon in While Statement" + " At Index:" + parseIndex(true), parseIndex(true));
        }
        return new Ast.Stmt.Return(value);
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
                    while(!match(")")){
                        list.add(parseExpression());
                        if (!peek(")")){
                            if(!peek(",")){
                                throw new ParseException("Expected Closed Parenthesis in Secondary Expression" + " At Index:" + parseIndex(true), parseIndex(true));
                            }
                            else if (peek(")")) {
                                throw new ParseException("Expected Comma Before Closing Parenthesis" + " At Index:" + parseIndex(true), parseIndex(true));
                            }
                            expr = new Ast.Expr.Function(Optional.of(expr), functionName, list);
                        }
                    }
                }
                else {
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
        else if (match(Token.Type.CHARACTER)) {
            if (tokens.get(-1).getLiteral().length() < 4) {
                Character character = tokens.get(-1).getLiteral().charAt(1);
                return new Ast.Expr.Literal(character);
            }
            else {
                String string = tokens.get(-1).getLiteral();
                string = string.replace("\\b", "\b");
                string = string.replace("\\r", "\r");
                string = string.replace("\\n", "\n");
                string = string.replace("\\t", "\t");
                string = string.replace("\\\"", "\"");
                string = string.replace("\\\\", "\\");
                string = string.replace("\\\'", "\'");

                Character character = string.charAt(1);
                return new Ast.Expr.Literal(character);
            }
        }

        else if (match(Token.Type.STRING)) {
            String string = tokens.get(-1).getLiteral();
            string = string.replace("\\b", "\b");
            string = string.replace("\\r", "\r");
            string = string.replace("\\n", "\n");
            string = string.replace("\\t", "\t");
            string = string.replace("\\\"", "\"");
            string = string.replace("\\\\", "\\");
            string = string.replace("\\\'", "\'");
            string = string.substring(1, string.length() - 1);
            match(Token.Type.STRING);
            return new Ast.Expr.Literal(string);
        }

        else if (peek("(")) {
            match("(");
            Ast.Expr.Group group = new Ast.Expr.Group(parseExpression());
            if (peek(")")) {
                match(")");
                return group;
            } else {
                if (tokens.has(0)) {
                    throw new ParseException("Invalid per no )" + " At Index:" + parseIndex(true), parseIndex(true));
                }
                else {
                    throw new ParseException("Invalid per no )" + " At Index:" + parseIndex(false), parseIndex(false));
                }
            }
        }

        else if (peek(Token.Type.IDENTIFIER)) {
            String name = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
            if (peek("(")) {
                match("(");
                List<Ast.Expr> arguments = new ArrayList<Ast.Expr>();
                while (!peek(")")) {
                    arguments.add(parseExpression());
                    if (peek(",")) {
                        match(",");
                        if (peek(")")) {
                            throw new ParseException("Invalid per trailing comma" + " At Index:" + parseIndex(true), parseIndex(true));
                        }
                    }
                }
                match(")");
                return new Ast.Expr.Function(Optional.empty(), name, arguments);
            }
            else {
                return new Ast.Expr.Access(Optional.empty(), name);
            }
        }
        else {
            if (tokens.has(0)) {
                throw new ParseException("invalid primary" + " INDEX:" + parseIndex(true), parseIndex(true));
            }
            else {
                    throw new ParseException("invalid primary" + " INDEX:" + parseIndex(false), parseIndex(false));
            }
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
