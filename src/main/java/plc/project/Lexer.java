package plc.project;

import org.omg.CORBA.IdentifierHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> tokenList = new ArrayList<>();
        while (chars.index <= chars.input.length()) {
            //skip over whitespace if negate is true
            if (peek("[^\b\r\n\t]")) {
                tokenList.add(lexToken());
                chars.advance();
            } else {
                //skip to the next in the string
                chars.skip();  //Length = 0
            }
        }
        return tokenList;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     * <p>
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     **/
    public Token lexToken() {
        if (peek("[A-Za-z_]")) {
            return lexIdentifier();
        } else if (peek("[+-0-9\\.]")) {
            return lexNumber();
        } else if (peek("\'")) {
            return lexCharacter();
        } else if (peek("\"")) {
            return lexString();
        } else if (peek("\\\\")) {
            lexEscape();
        } else {
            return lexOperator();
        }
        throw new ParseException("Error exists at index " + chars.index, chars.index);
    }


    public Token lexIdentifier() {
            String tokenString = new String(); //Creates string that will become the token data
            while(peek("[A-Za-z0-9_-]*")) {  //This loop will continue for the full length of identifier
                tokenString += chars.get(chars.index); //Adds the current char from charstring then...
                chars.advance();                       //skips to the next character and loops
            }
            Token token = new Token(Token.Type.IDENTIFIER, tokenString, tokenString.length());
            //Creates a token of type identifier containing this string of characters
            return token;
            //returns it
    }

    public Token lexNumber() {
        /** Work in progress
        int startIndex = chars.index;
        while(peek("[+-0-9\\.]"))

            if (peek(("[0-9]"))) {
            Token token = chars.emit(Token.Type.INTEGER);
            return token;
        }
        else if (peek(("\\."))) {
            Token token = chars.emit(Token.Type.DECIMAL);
            return token;
        }
        //Add condition for final decimal
        return null;
         */
        throw new UnsupportedOperationException(); //TODO
    }

    public Token lexCharacter() {
        throw new UnsupportedOperationException(); //TODO
    }

    public Token lexString() {
        match("\"");
        while(match("[^\"\r\n]")) {
            if(match("\\\\")) {
                if(!match("[brnt\"'\\\\]")) {
                    throw new ParseException("Not Valid",chars.index);
                }
            }
        }
        if (match("\"")) {
            return chars.emit(Token.Type.STRING);
        }

        throw new UnsupportedOperationException();
    }

    public void lexEscape() {
        if (match("\\\\")) {
            if (match("[bnrt\\\"\\'\\\\]")) {
                return;
            }
        }
        throw new ParseException("Escape Not Valid at index ", chars.index);
    }

    public Token lexOperator() {
        if (match("[!=<>]")) {
            if (match("==")) {
                return chars.emit(Token.Type.OPERATOR);
            }
            else if (match("<=")) {
                return chars.emit(Token.Type.OPERATOR);
            }
            else if (match(">=")) {
                return chars.emit(Token.Type.OPERATOR);
            }
        }
        match(".");
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */

    public boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as peek String but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     **/


    public boolean match(String... patterns) {
        boolean matched = peek(patterns); //Calls back to peek
        if (matched) {
            for (int i = 0; i < patterns.length; i++) {
                chars.advance();
            }
        }
        return matched;
    }


    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     * <p>
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }
    }
}
