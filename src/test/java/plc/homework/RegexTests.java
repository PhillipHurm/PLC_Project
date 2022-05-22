package plc.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. A framework of the test structure 
 * is provided, you will fill in the remaining pieces.
 *
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                //Examples
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),

                //Piece 1: [A-Za-z0-9._-]+
                Arguments.of("Capital Letter Gmail", "P@gmail.com", true),
                Arguments.of("Lowercase Letter Gmail", "d@gmail.com", true),
                Arguments.of("Number Gmail", "7@gmail.com", true),
                Arguments.of("Period Gmail", ".@gmail.com", true),
                Arguments.of("Underscore Gmail", "A@gmail.com", true),
                Arguments.of("Hyphen", "-@gmail.com", true),
                Arguments.of("All Allowed Characters Gmail", "ABC123._-@gmail.com", true),
                Arguments.of("Missing Character Gmail", "@gmail.com", false),
                Arguments.of("Pipe Symbol Gmail", "|@gmail.com", false),
                Arguments.of("Asterisk Gmail", "*@gmail.com", false),

                //Piece 2: @
                Arguments.of("One @ Gmail", "ABC123._-@gmail.com", true),
                Arguments.of("Missing @ Gmail", "ABC123._-gmail.com", false),
                Arguments.of("Double @ Gmail", "ABC123._-@@gmail.com", false),

                //Piece 3: [A-Za-z0-9-]*
                Arguments.of("ABC123._-@Capital Letter", "ABC123._-@T.com", true),
                Arguments.of("ABC123._-@Lowercase Letter", "ABC123._-@g.com", true),
                Arguments.of("ABC123._-@Number", "ABC123._-@5.com", true),
                Arguments.of("ABC123._-@Hyphen", "ABC123._-@-.com", true),
                Arguments.of("ABC123._-@Missing", "ABC123._-@.com", true),
                Arguments.of("ABC123._-@All Allowable Characters", "ABC123._-@ABC123-.com", true),

                //Piece 4: \.
                Arguments.of("One .", "ABC123._-@gmail.com", true),
                Arguments.of("Missing .", "ABC123._-@gmailcom", false),
                Arguments.of("Double .", "ABC123._-@gmail..com", false),

                //Piece 5: [a-z]{2,3}
                Arguments.of("Gmail. Lowercase x2", "ABC123._-@gmail.cd", true),
                Arguments.of("Gmail. Lowercase x3", "ABC123._-@gmail.fgh", true),
                Arguments.of("Gmail. Missing", "ABC123._-@gmail.", false),
                Arguments.of("Gmail. Lowercase x1", "ABC123._-@gmail.r", false),
                Arguments.of("Gmail. Lowercase x4", "ABC123._-@gmail.comy", false),
                Arguments.of("Gmail. Uppercase x3", "ABC123._-@gmail.AB", false),
                Arguments.of("Gmail. Number x3", "ABC123._-@gmail.123", false),
                Arguments.of("Gmail. Symbols x3", "ABC123._-@gmail.***", false)
                );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //Examples
                //What has ten letters and starts with gas?  <<<Nice dad joke!
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),

                //True Tests
                Arguments.of("10 Uppercase", "AUTOMOBILE", true),
                Arguments.of("10 Lowercase", "automobile", true),
                Arguments.of("10 Numbers", "1234567890", true),
                Arguments.of("10 Symbols", "!@#$%^&*()", true),
                Arguments.of("10 Mixed", "Sd1@#$%^&*", true),
                Arguments.of("10 Repeating", "aaaaaaaaaa", true),
                Arguments.of("16 Mixed", "Sd1@#$%^&*(<.:}{", true),
                Arguments.of("20 Mixed", "Sd1@#$%^&*(<.:}{mM6+", true),

                //False Tests
                Arguments.of("0 Characters", "", false),
                Arguments.of("8 Characters Mixed", "sD1$%^&*", false),
                Arguments.of("9 Characters Mixed", "sD1$%^&*(", false),
                Arguments.of("11 Characters Mixed", "sD1$%^&*(._", false),
                Arguments.of("19 Characters Mixed", "sD1$%^&*(._<>,./?:+", false),
                Arguments.of("21 Characters Mixed", "sD1$%^&*(._<>,./?:+$%", false),
                Arguments.of("22 Characters Mixed", "sD1$%^&*(._<>,./?:+$%d", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        //Because the space is only specified to be allowed after the comma, I did not allow leading spaces or space
        //between the digit and comma.  I did not include zero as it is not a positive integer.
        return Stream.of(
                Arguments.of("Empty Set", "[]", true),
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements Sequential", "[1,2,3]", true),
                Arguments.of("Multiple Elements Random", "[4,7,6]", true),
                Arguments.of("Multiple Elements with Repeats", "[1,1,1]", true),
                Arguments.of("Multiple Elements with Single Spaces After Comma", "[1, 2, 3]", true),
                Arguments.of("Multiple Elements with Mixed Spaces After Comma", "[1,2, 3,        4]", true),

                Arguments.of("Single Element Number", "[A]", false),
                Arguments.of("Single Element Symbol", "[*]", false),
                Arguments.of("Multiple Elements With One Letter", "[1,2,3,A]", false),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Leading Bracket", "1,2,3]", false),
                Arguments.of("Missing Trailing Bracket", "[1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Missing Commas No Space", "[123]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false),
                Arguments.of("Zero", "[0]", false),
                Arguments.of("Zero in list", "[1,2,3,0]", false),
                Arguments.of("Leading space", "[ 1,2,3]", false),
                Arguments.of("Space before comma", "[1 ,2 ,3 ,]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        return Stream.of(
                Arguments.of("Single Digit", "1", true),
                Arguments.of("Multiple Digits", "12495816507519", true),
                Arguments.of("Decimal After Initial Integer", "1.07", true),
                Arguments.of("Decimal In Middle", "1548390.0943", true),
                Arguments.of("Positive Sign", "+1", true),
                Arguments.of("Negative Sign", "-1", true),
                Arguments.of("Leading Zeroes", "000001", true),
                Arguments.of("Trailing Zeroes", "100000", true),
                Arguments.of("Negative With Leading Zeroes", "-000001", true),
                Arguments.of("Multiple Digits With Negative And Decimal In Middle", "-12.567", true),

                Arguments.of("Empty", "", false),
                Arguments.of("Letter", "A", false),
                Arguments.of("Symbol", "*", false),
                Arguments.of("Long Integer With Single Letter", "1245657A45", false),
                Arguments.of("Trailing Positive Sign", "1+", false),
                Arguments.of("Trailing Negative Sign", "1-", false),
                Arguments.of("Leading Decimal", ".1", false),
                Arguments.of("Trailing Decimal", "1.", false),
                //This last case I believe should be false.  The way the specification is written, this would still
                //qualify as a leading decimal with an optional plus or minus sign.
                Arguments.of("Leading Decimal After Negative", "-.1", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        return Stream.of(

                Arguments.of("Number", "\"1\"", true),
                Arguments.of("Letter", "\"A\"", true),
                Arguments.of("Symbol", "\"*\"", true),
                Arguments.of("Mixed Characters", "\"1A*\"", true),
                Arguments.of("Backslash b", "\"\\b\"", true),
                Arguments.of("Backslash n", "\"\\n\"", true),
                Arguments.of("Backslash r", "\"\\r\"", true),
                Arguments.of("Backslash t", "\"\\t\"", true),
                Arguments.of("Backslash Single Quotation", "\"\\'\"", true),
                Arguments.of("Backslash Double Quotation", "\"\\\"\"", true),
                Arguments.of("Backslash Backslash", "\"\\\\\"", true),
                Arguments.of("Empty Quotation Marks", "\"\"", true),
                Arguments.of("Hello, World!", "\"Hello, World!\"", true),
                Arguments.of("Example", "\"1\\t2\"", true),

                Arguments.of("Backslash x", "\"\\x\"", false),
                Arguments.of("Backslash Forward Slash", "\"\\/\"", false),
                Arguments.of("No Quotation Marks", "ABC123&", false),
                Arguments.of("Single Backslash Without Following Character", "\"\\\"", false),
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("No Initial Quotation Mark", "Whoops!\"", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }

}
