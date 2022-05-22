package plc.homework;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regexes as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._\\-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            EVEN_STRINGS = Pattern.compile("(.{2}){5,10}"),
    //My original solution was "(.{2})|.{12}|.{14}|.{16}|.{18}|.{20}" but I am proud that I used the example to find
    //this far more elegant and easily extendable solution.
            INTEGER_LIST = Pattern.compile("\\[(([1-9])(,\\s*[1-9])*)?\\]"),
            NUMBER = Pattern.compile("[+-]?\\d+(\\.\\d+)?"),
            STRING = Pattern.compile("\"([^\\\\]|\\\\b|\\\\n|\\\\r|\\\\t|\\\\'|\\\\\"|\\\\\\\\)*\"");

}
