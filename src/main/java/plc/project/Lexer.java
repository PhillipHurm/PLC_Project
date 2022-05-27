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
            if (peek("[^\\s\b\r\n\t]")) {
                tokenList.add(lexToken());
            } else {
                chars.advance();
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
        } else if (peek("[+\\-]","[0-9]") || peek("[0-9]")) {
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
        if (match("[A-za-z_]")) {
            while (match("[A-za-z0-9_\\-]")) {
            }
            return chars.emit(Token.Type.IDENTIFIER);
        }
        else throw new ParseException("Not Valid",chars.index);
    }

    public Token lexNumber() {
        Token.Type tokenType = Token.Type.INTEGER;
        boolean decimalFlag = false;
        boolean multiDecimalChecker = false;
    /*    if(peek("\\.")) {
            throw new ParseException("Not Valid",chars.index);
        }*/
        match("[+-]");
        while (match("[0-9]")) {
            if (match("\\.", "[0-9]")) {
                while (match("[0-9]")) {
                }
                return chars.emit(Token.Type.DECIMAL);
            }
        }
        return chars.emit(Token.Type.INTEGER);
           /* if(decimalFlag == true)
                    if(!peek("[0-9]"))
                        throw new ParseException("Not Valid",chars.index);
            if(peek("\\.")) {
                tokenType = Token.Type.DECIMAL;
                decimalFlag = true;
                if (multiDecimalChecker==false)
                    multiDecimalChecker = true;
                else
                    throw new ParseException("Not Valid",chars.index);
            }
            else
                decimalFlag = false;
        }
            return chars.emit(tokenType);
            */

    }

    public Token lexCharacter() {
            match("\'");
           /* if (match("\\\\", "[bnrt'\"\\\\]", "\'")) {
                    return chars.emit(Token.Type.CHARACTER);
            }*/
            if (peek("[^\'\r\n]")) {
                if (peek("\\\\")) {
                    lexEscape();
                } else {
                    chars.advance();
                }
            }
            else {
                throw new ParseException("Not Valid",chars.index);
            }
            if (!match("\'")) {
                throw new ParseException("Not Valid", chars.index);
            }

        return chars.emit(Token.Type.CHARACTER);
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
        else {
            throw new ParseException("Not Valid",chars.index);
        }
    }

    public void lexEscape() {
        match("\\\\");
        if (!match("[bnrt\"\'\\\\]")) {
            throw new ParseException("Not Valid ", chars.index);
        }
    }

    public Token lexOperator() {
        if (match("[<>!=]")) {
            match("=");
        } else {
            chars.advance();
        }
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
