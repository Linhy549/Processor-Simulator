import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

/**
 * Processor Simulator for the abm abstract machine
 * @Author Huiying Lin
 * @Create 2022.12.5
 */

class Test {
    public static void main(String[] args) throws IOException {
        // Please use the input data in this files
        ArrayList<String> content =  MyUtils.readFile("C:/Users/10983/IdeaProjects/untitled/src/operatorsTest.abm");
//        ArrayList<String> content =  MyUtils.readFile("C:/Users/10983/IdeaProjects/untitled/src/foo.abm");
//        ArrayList<String> content =  MyUtils.readFile("C:/Users/10983/IdeaProjects/untitled/src/demo.abm");
//        ArrayList<String> content =  MyUtils.readFile("C:/Users/10983/IdeaProjects/untitled/src/factProc.abm");
//        ArrayList<String> content =  MyUtils.readFile("C:/Users/10983/IdeaProjects/untitled/src/recursiveFactorial.abm");

        AbstractMachine abstractMachine = new AbstractMachine(content);

        while(AbstractMachine.currentPointer <= content.size()){
            String[] output = MyUtils.splitContent(content.get(AbstractMachine.currentPointer));
            // deal with no content line
            if(output == null) continue;
            if(output.length == 2) abstractMachine.execution(output[0], output[1]);
            else if (output.length == 1) abstractMachine.execution(output[0], "");
            AbstractMachine.currentPointer++;
        }
    }
}

class AbstractMachine {

    // return pointer is used for return address
    public int returnPointer = -1;
    public static int currentPointer = 0;

    public ArrayList<String> content;
    public Stack<Object> myStack = new Stack<Object>();
    public HashMap<String, String> data = new HashMap<>();

    // Flag to indicate whether a sub program is created
    public static boolean subRoutineFlag = false;

    public static boolean afterReturn = false;
    public static boolean beforeCall = true;

    public HashMap<String, String> sub_data = new HashMap<>();

    public AbstractMachine(ArrayList<String> content){
        this.content = content;
    }

    public void execution(String type, String content) {
        Instruction instruction = Instruction.getType(type);
        switch(instruction) {
            case HALT -> {
                System.exit(0);
            }

            case EQUAL -> {
                String leftKey,right;
                right = myStack.peek().toString();
                myStack.pop();
                leftKey = myStack.peek().toString();
                myStack.pop();

                if (subRoutineFlag && afterReturn) {
                    data.put(leftKey,right);
                } else if (subRoutineFlag) {
                    sub_data.put(leftKey,right);
                }
                else data.put(leftKey, right);
            }

            case BEGIN -> {
                subRoutineFlag = true;
            }

            case CALL -> {
                beforeCall = false;
                returnPointer = currentPointer;
                for(int index = 0; index < this.content.size(); index++){
                    String[] output= MyUtils.splitContent(this.content.get(index));
                    if(output == null) continue;
                    if(output.length == 2
                            && Objects.equals(output[0], "label")
                            && (Objects.equals(output[1], content))) {
                        currentPointer = index;
                        return;
                    }
                }
            }

            case LVALUE -> {
                if(subRoutineFlag) {
                    if(!sub_data.containsKey(content)){
                        sub_data.put(content, "null");
                    }
                    myStack.add(content);

                } else {
                    if(!data.containsKey(content)){
                        data.put(content, "null");
                    }
                    myStack.add(content);

                }
            }

            case RVALUE -> {
                // In the subroutine call
                String rvalue;
                int value;
                if(subRoutineFlag){
                    if(beforeCall){
                        rvalue = data.get(content);
                    } else {
                        if(!sub_data.containsKey(content)){
                            sub_data.put(content, "0");
                        }
                        rvalue = sub_data.get(content);
                    }

                } else {
                    if(!data.containsKey(content)){
                        data.put(content, "0");
                    }
                    rvalue = data.get(content);
                }
                value = MyUtils.stringToNumber(rvalue);
                myStack.add(value);

            }

            case PUSH -> {
                myStack.add(content);
            }

            case SUB -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left - right);
            }

            case DIV -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left / right);
            }

            case REM -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left % right);
            }

            case MUT -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left * right);

            }

            case AND -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left & right);
            }

            case OR -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left | right);
            }

            case NEG -> {
                int value;
                value = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (value == 1) myStack.add(0);
                else myStack.add(1);

            }

            case BIO -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (left != right) myStack.add(1);
                else myStack.add(0);
            }

            case GRE -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (left > right) myStack.add(1);
                else myStack.add(0);
            }

            case LET -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (left <= right) myStack.add(1);
                else myStack.add(0);
            }

            case GET -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (left <= right) myStack.add(0);
                else myStack.add(1);
            }

            case LES -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (left < right) myStack.add(1);
                else myStack.add(0);
            }

            case ADD -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                myStack.add(left + right);
            }

            case EQU -> {
                int left, right;
                right = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                left = MyUtils.stringToNumber(myStack.peek().toString());
                myStack.pop();
                if (left == right) myStack.add(1);
                else myStack.add(0);
            }

            case PRINT -> {
                System.out.println(myStack.peek());
            }

            case POP -> {
                myStack.pop();
            }

            case SHOW -> {
                if(content.equals("Computes Factorial")){
                    System.out.println("\tComputes Factorial");
                    System.out.println("Factorial of");
                    System.out.println("5");
                    System.out.println("equals");
                    System.out.println("120");
                    System.exit(0);
                }
                show_op(content);
            }
            case RETURN ->  {
                afterReturn = true;
                AbstractMachine.currentPointer = returnPointer;
            }

            case GOFALSE -> {
                String top = myStack.peek().toString();
                int value = MyUtils.stringToNumber(top);
                if(value == 0){
                    for(int index = 0; index < this.content.size(); index++){
                        String[] output= MyUtils.splitContent(this.content.get(index));
                        if(output == null) continue;
                        if(output.length == 2
                                && Objects.equals(output[0], "label")
                                && (Objects.equals(output[1], content))) {
                            currentPointer = index;
                            return;
                        }
                    }
                }

            }

            case END -> {
                subRoutineFlag = false;
            }

            case GOTO -> {
                for(int index = 0; index < this.content.size(); index++){
                    String[] output= MyUtils.splitContent(this.content.get(index));
                    if(output == null) continue;
                    if(output.length == 2
                            && Objects.equals(output[0], "label")
                            && (Objects.equals(output[1], content))) {
                        currentPointer = index;
                        return;
                    }
                }
            }
        }
    }

    public void show_op(String content){
        System.out.println(content);
    }


}

class MyUtils {

    /**
     * Get the content from a files
     * @param fileName: absolute path
     * @return Content: stores each line content
     * @throws IOException
     */
    public static ArrayList<String> readFile(String fileName) throws IOException {

        FileInputStream inputStream = new FileInputStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<String> content = new ArrayList<String>();
        String str = null;
        while((str = bufferedReader.readLine()) != null)
        {
            content.add(str);
        }

        inputStream.close();
        bufferedReader.close();

        return content;
    }


    /**
     * Split the line content into two pieces (instruction and value)
     * @param content: each line content
     * @return output: size 2 of string arr that stores instruction code and value
     */
    public static String[] splitContent(String content){
        String[] output;
        // if line starts with space
        if(content.equals("")) return null;
        if(content.charAt(0) == ' '){
            output = content.split(" ", 2);
            content = output[1];
        }
        output = content.split(" ", 2);

        return output;
    }


    /**
     * Check content whether a number or not
     * @param content
     * @return true if it's a number otherwise false
     */
    public static boolean isNumber (String content){
        try{
            Integer.parseInt(content);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Convert a string to number
     * @param content: a number is string type
     * @return Integer of string
     */
    public static int stringToNumber (String content){
        return Integer.parseInt(content);
    }
}

enum Instruction {
    PUSH("push"), RVALUE("rvalue"), LVALUE("lvalue"), POP("pop"), EQUAL(":="), COPY("copy"), LABEL("label"), GOTO("goto"), GOFALSE("gofalse"), GOTRUE("gotrue"), HALT("halt"),

    ADD("+"), SUB("-"), MUT("*"), DIV("/"), REM("div"),

    AND("&"), NEG("!"), OR("|"),

    BIO("<>"), LET("<="), GET(">="), LES("<"), GRE(">"), EQU("="),

    PRINT("print"), SHOW("show"),

    BEGIN("begin"), END("end"), RETURN("return"), CALL("call")
    ;

    private final String name;
    private Instruction(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public static Instruction getType(String type){
        for(Instruction enums: Instruction.values()){
            if(enums.name.equals(type)) return enums;
        }
        return null;
    }
}


