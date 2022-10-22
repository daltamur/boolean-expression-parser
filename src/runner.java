import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class runner {
    //Use this hashmap to keep track of variable values
    public static HashMap<Character, Boolean> variableValues = new HashMap<>(26);
    public static void main(String[] args) throws Exception {
        parser p = new parser(new lexer());
        p.parse();
        runForever(p);
    }

    public static void runForever(parser p) throws Exception {
        p.lexer.clearScannerVals();
        p.lexer.next();
        p.parse();
        runForever(p);
    }
}

class lexer{
    static final char END = '$';
    private char yytext;
    private char token;
    private final Scanner scanner = new Scanner(System.in);
    private char errorVal;

    private int lexPos=0;

    public lexer() throws IOException {
        scanner.useDelimiter("");
        next();}

    public void clearScannerVals(){
        yytext = ' ';
        token = ' ';
        lexPos = 0;
    }

    public void resetScanner() throws IOException {
        if(token!=END){
            next();
            resetScanner();
        }
    }
    public void next() throws IOException {
        if(token!=END) {
            String currentExpression = scanner.next();
            yytext = currentExpression.charAt(0);
            lexPos++;
            errorVal = yytext;
            if(errorVal == '\n'){
                errorVal = '$';
            }
            if (Pattern.compile("[a-z]").matcher((Character.toString(yytext))).find()) {
                if (!runner.variableValues.containsKey(yytext)) {
                    runner.variableValues.put(yytext, false);
                }
                token = yytext ;

            } else {
                switch (yytext) {
                    case '\n':
                        yytext = token = END;
                    case ' ':
                        next();
                    case '(':
                        token = yytext;
                    case ')':
                        token = yytext;
                    case '^':
                        token = yytext;
                    case '|':
                        token = yytext;
                    case '&':
                        token = yytext;
                    case '?':
                        token = yytext;
                    case '=':
                        token = yytext;
                    case '~':
                        token = yytext;
                    case '0':
                        token = yytext;
                    case '1':
                        token = yytext;
                    case '/':
                        System.exit(0);
                }
            }
        }
    }

    char matchLetter() throws IOException {
        char lexValue;
        if(Pattern.compile("[a-z]").matcher((Character.toString(token))).find()){
            lexValue = token;
            next();
        }else{
            lexValue = 0;
        }
        return lexValue;
    }

    char checkLetter() throws IOException {
        char lexValue;
        if(Pattern.compile("[a-z]").matcher((Character.toString(token))).find()){
            lexValue = token;
        }else{
            lexValue = 0;
        }
        return lexValue;
    }

    char getToken(){
        return token;
    }
    char getErrorVal(){
        return errorVal;
    }

    int getLexPos(){
        return lexPos;
    }

}

class parser{
    lexer lexer;
    parser(lexer lexer){this.lexer=lexer;}

    void parse() throws Exception {S();}

    private void S() throws Exception {
        //since we're dealing with any possible letter, we're just gonna do a regex check
        char curVariableLetter = lexer.getToken();
        //System.out.println("Starting token: "+curVariableLetter);
        try {
            if (lexer.matchLetter() != 0) {

                //test for assign or query
                char curChar = lexer.getToken();

                //query node recognized
                if (curChar == '?') {
                    //Just handle the query operations here

                    //get the value of the variable letter that was asked for from the hashmap
                    if (runner.variableValues.containsKey(curVariableLetter)) {
                        int x = runner.variableValues.get(curVariableLetter) ? 1 : 0;
                        System.out.println(x);
                    } else {
                        runner.variableValues.put(curVariableLetter, false);
                        System.out.println(0);
                    }

                    //progress passed the ? character
                    lexer.next();

                //assign node recognized
                } else if (curChar == '=') {
                    //just handle the assign operations here

                    //progress passed the equals character
                    lexer.next();

                    //evaluate the E1 expression the variable is being assigned to
                    Boolean newVal = E1();

                    //so long as the evaluation of E1 didn't throw an error, put the new value of the given variable in the hashmap
                    if(newVal != null) {
                        runner.variableValues.put(curVariableLetter, newVal);
                    }
                    //System.out.println("done");
                }  else {
                    //equals anything else, we have an error
                    throw new Exception("Error: expected = or ?, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
                }
                //System.out.println("At "+lexer.getToken());
            }else{
                throw new Exception("Error: expected variable letter, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }

            //at the very end of this, we should be at an EOL character. If we aren't something messed up along the way.
            if(lexer.getToken() != '$'){
                throw new Exception("Error: expected EOL, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }

        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
        }
    }

    private Boolean E1() throws IOException {
        Boolean curBoolean = false;
        try {
            //check for the members of E1's first set
            if (lexer.getToken() == '~' | lexer.getToken() == '(' | lexer.getToken() == '0' | lexer.getToken() == '1' | lexer.checkLetter() != 0) {
                //go on to evaluate the E2 node
                Boolean E2Val = E2();
                curBoolean = E2Val;
            }else {
                throw new Exception("Error: expected ~ or ( or 0 or 1 or variable letter, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }

            //if evaluating the E2 node didn't have an error somewhere along the way, go on to evaluate the E1Tail node
            if(curBoolean != null) {
                curBoolean = E1Tail(curBoolean);
            }
            return curBoolean;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }

    private Boolean E1Tail(Boolean curBoolean) throws IOException {
        try {
            //check for first set of E1Tail
            if (lexer.getToken() == '|') {
                //index past the or character
                lexer.next();

                //Evaluate the right side of the or expression
                Boolean E1Val = E1();

                //if the E1 evaluation didn't return an error, perform the or operation
                if (E1Val != null) {
                    curBoolean = curBoolean | E1Val;
                } else {
                    curBoolean = null;
                }

                //if this was fruitless, check for the follow set of E1Tail to see if we still have a legal expression
            } else if (lexer.getToken() == '$' | lexer.getToken() == ')') {

            } else {
                throw new Exception("Error: expected | or ) or EOL token, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }
            return curBoolean;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }

    private Boolean E2() throws IOException {
        try {
            Boolean curBoolean = false;

            //check for the first set of E2
            if (lexer.getToken() == '~' | lexer.getToken() == '(' | lexer.getToken() == '0' | lexer.getToken() == '1' | lexer.checkLetter() != 0) {
                //go on to evaluate the E3 node
                curBoolean = E3();
            } else {
                throw new Exception("Error: expected ~ or ( or 0 or 1 or variable letter, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }

            //if the E3 evaluation didn't return an error, evaluate the E2Tail node
            if(curBoolean != null) {
                curBoolean = E2Tail(curBoolean);
            }
            return curBoolean;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }

    private Boolean E2Tail(Boolean curBoolean) throws IOException {

        //check for E2Tail first set
        try {
            if (lexer.getToken() == '^') {

                //index passed XOR character
                lexer.next();

                //evaluate the E2 node that would be on the right side of the XOR operation
                Boolean E2Val = E2();

                ////if the E2 evaluation didn't return an error, perform the or operation
                if (E2Val != null) {
                    curBoolean = curBoolean ^ E2Val;
                } else {
                    curBoolean = null;
                }

                //check for the follows sets in case the first sets weren't recognized to make sure we still have a legal character
            } else if (lexer.getToken() == '|' | lexer.getToken() == '$' | lexer.getToken() == ')') {

            } else {
                throw new Exception("Error: expected | or ( or EOL token, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }
            return curBoolean;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }

    private Boolean E3() throws IOException {
        try {
            Boolean curBoolean = false;

            //check E3s first set
            if (lexer.getToken() == '(' | lexer.getToken() == '0' | lexer.getToken() == '1' | lexer.checkLetter() != 0 | lexer.getToken() == '~') {
                //go on to evaluate the term node
                curBoolean = T();
            } else {
                throw new Exception("Error: expected ~ or ( or 0 or 1 or variable letter, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }
            return curBoolean;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }

    private Boolean T() throws IOException {
        try {
            Boolean value = false;
            //check for not character
            if (lexer.getToken() == '~') {
                //progress passed not character
                lexer.next();

                //evaluate the T node attached to the not character
                value = T();

                //if the evaluation of the T node didn't return an error, negate the returned value
                if(value != null) {
                    value = !value;
                }

                //check if the current character is FALSE
            } else if (lexer.getToken() == '0') {
                //set the returned value of T to false
                value = false;

                //progress passed the FALSE character
                lexer.next();

                //check if the current character is TRUE
            } else if (lexer.getToken() == '1') {
                //set the returned value to true
                value = true;

                //progress passed the TRUE character
                lexer.next();

                //check if the current character is a variable letter
            } else if (lexer.checkLetter() != 0) {
                //if the current variable letter already exists in our hashtable, return the value that it is currently associated with.
                if (runner.variableValues.containsKey(lexer.getToken())) {
                    value = runner.variableValues.get(lexer.getToken());
                } else {
                    //if the current variable letter does not exist in our hashtable, set it to false and return this false value
                    runner.variableValues.put(lexer.getToken(), false);
                    value = false;

                }

                //progress passed the variable letter character
                lexer.next();

                //check if the current character is an open parentheses
            } else if (lexer.getToken() == '(') {
                //progress passed the ( character
                lexer.next();
                //evaluate the contents of the parentheses
                value = E1();

                //if the evaluation does not return a null value, make sure the user closed the parentheses
                if(value != null) {
                    if (lexer.getToken() != ')') {
                        throw new Exception("Error: expected ), instead had " + lexer.getErrorVal() + " at index " + lexer.getLexPos());
                    }
                }

                //progress passed the closed parentheses character if there was no error
                lexer.next();
            }else {
                throw new Exception("Error: expected ( or 0 or 1 or variable letter, instead had "+lexer.getErrorVal()+" at index "+lexer.getLexPos());
            }

            //if at some point the evaluation did not fail, go on to evaluate the TTail node
            if(value != null) {
                value = TTail(value);
            }

            return value;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }

    private Boolean TTail(Boolean value) throws Exception {
        try {

            //check if the current character is the and character
            if (lexer.getToken() == '&') {

                //progress passed the and character
                lexer.next();

                //evaluate the T node attached to the and expression
                Boolean TVal = T();

                //if the evaluation of the T node did not lead to an error, perform the and operation
                if (TVal != null) {
                    value = value & TVal;
                } else {
                    value = null;
                }

                //if the firs set check was fruitless, check the follows set to make sure we are still on a legal character.
            } else if (lexer.getToken() == '$'|lexer.getToken()=='^'|lexer.getToken()=='|'|lexer.getToken()==')') {

            } else {
                throw new Exception("Error: expected & or ^ or | or EOL character, instead had " + lexer.getErrorVal() + " at index " + lexer.getLexPos());
            }

            return value;
        }catch (Exception e){
            System.out.println(e);
            lexer.resetScanner();
            return null;
        }
    }


}
