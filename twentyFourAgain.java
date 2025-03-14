
/**
 * Write a description of class twentyFourAgain here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
import java.io.*;
import java.util.*;
import jdk.jshell.*;

public class twentyFourAgain
{
    private static int permCount = 0;
    private static int lowestGrade = Integer.MAX_VALUE;

    public static void main(String[] args) 
    {
        // Load Base Value
        Scanner input = new Scanner(System.in);

        int[] originValues = new int[4];
        originValues[0] = input.nextInt();
        originValues[1] = input.nextInt();
        originValues[2] = input.nextInt();
        originValues[3] = input.nextInt();
        //Feed Brute Swap and start the suffering.
        //Small opt
        BruteSwaps(originValues);
        if(lowestGrade != Integer.MAX_VALUE){
            System.out.print(lowestGrade);
        } else {
            System.out.print("impossible");
        }

    }

    private static void BruteSwaps(int[] originValues)
    {
        ArrayList<int[]> comboList = new ArrayList<>();
        //Queue
        Stack<int[]> currentArr = new Stack<>();
        Stack<Integer> currentSwaps = new Stack<>();

        ArrayList<int[]> seen = new ArrayList<>();
        ArrayList<Integer> scores = new ArrayList<>();

        currentArr.push(originValues.clone());
        currentSwaps.push(0);

        int score = 0;

        while(!currentArr.empty() && !currentSwaps.empty())
        {
            int[] currentArray = currentArr.pop().clone();
            int swapCount = currentSwaps.pop();

            //Contains Flag
            boolean seenContains = false;
            if(seen.size()>0)
            {
                for(int i = 0; i < seen.size(); i++)
                {
                    if(Arrays.equals(seen.get(i), currentArray))
                        seenContains = true;
                }
            }

            if( !seenContains )
            {
                comboList.add(currentArray);
                seen.add(currentArray);
                scores.add(swapCount*2);

                for(int i = 0; i < currentArray.length - 1; i++)
                {
                    int[] newArray = currentArray.clone();
                    int temp = newArray[i+1];
                    newArray[i+1] = newArray [i];
                    newArray[i] = temp;

                    seenContains = false;
                    if(seen.size()>0)
                    {
                        for(int j = 0; j < seen.size(); j++)
                        {
                            if(Arrays.equals(seen.get(j), newArray))
                            {
                                seenContains = true;
                                break;
                            }
                        }
                    }

                    if(!seenContains)
                    {
                        currentArr.push(newArray);
                        currentSwaps.push(swapCount+1);
                    }
                }
            }
        }

        int index = 0;
        for(int[] valArr : seen)
        {
            int pass = scores.get(index);
            index++;
            BruteArithmetic(valArr, pass);

        }
    }
    
    //Function that helps compare arrays
    private static void BruteArithmetic(int[] baseValues, int score) 
    {

        String pos3  = "+-*/";
        String pos7  = "+-*/";
        String pos11 = "+-*/";

        // 012345678901234
        //"(5)+(4)+(5)+(4)"
        //Possible Pairings
        //(x + x)+ x + x 
        // x +(x + x)+ x 
        // x + x +(x + x)
        //(x + x)+(x + x)
        //(x + x + x)+ x
        // x +(x + x + x)
        int originalscore = score;

        int[] valArr = baseValues.clone();

            String[] groupings = {
            "%s %c %s %c %s %c %s",     //No Parenthesis
            "(%s %c %s) %c %s %c %s",
            "%s %c %s %c (%s %c %s)",
            "(%s %c %s %c %s) %c %s",
            "%s %c (%s %c %s %c %s)",
            "(%s %c %s) %c (%s %c %s)",
            "((%s %c %s) %c) %s %c %s",
            "%s %c (%s %c (%s %c %s))",
            "(%s %c (%s %c %s)) %c %s",
            "%s %c ((%s %c %s) %c %s)"
        };
        String operators = "+-*/";

        for(String group: groupings)
        {
            for(int i0 = 0; i0 < operators.length(); i0++)
            {
                for(int i1 = 0; i1 < operators.length(); i1++)
                {
                    for(int i2 = 0; i2 < operators.length(); i2++)
                    {
                        //This is so cool
                        char op1 = operators.charAt(i0);
                        char op2 = operators.charAt(i1);
                        char op3 = operators.charAt(i2);

                        //Snag ints
                        String val1 = Integer.toString(valArr[0]);
                        String val2 = Integer.toString(valArr[1]);
                        String val3 = Integer.toString(valArr[2]);
                        String val4 = Integer.toString(valArr[3]);

                        //Format that shit....
                        String expr = String.format(group, val1, op1, val2, op2, val3, op3, val4);

                        int count = 0;

                        for(int c = 0; c < expr.length(); c++)
                        {
                            if(expr.charAt(c) == ')') count++;
                        }

                        Integer result = loadStack(expr);
                        if(result != null && result == 24)
                        {
                            lowestGrade = Math.min(lowestGrade, originalscore + count);
                            System.out.println(expr + " = " + result + ". Grade" + (originalscore + count));
                        }
                    }
                }
            }
        }
    }

    //Have to move everything to the open... gross.

    public static Integer loadStack(String expression)
    {
        //Shunting Yard Algorithm
        //https://en.wikipedia.org/wiki/Shunting_yard_algorithm
        //tokenize the expression.
        char[] token = expression.toCharArray();

        Stack<Integer> values   = new Stack<>();
        Stack<Character> ops    = new Stack<>();

        //Go through char array 1 token at a time.

        for(int i = 0; i < token.length; i++)
        {
            //Read each token
            char c = token[i];
            if (c == ' ') continue;

            if(Character.isDigit(c)){
                StringBuilder val = new StringBuilder();
                //Detect any digits after.
                while(i < token.length && Character.isDigit(token[i]) ){
                    val.append(token[i]);
                    i++;
                }

                values.push(Integer.parseInt(val.toString()));
                i--;
            }else if (c == '(') 
            {
                ops.push(c);
            }else if (c == ')') 
            {
                while(!ops.isEmpty() && ops.peek() != '(')
                {
                    if(values.size() < 2) return null;
                    Integer result = resolveNext(ops.pop(), values.pop(), values.pop());
                    if (result == null) return null;
                    values.push(result);
                }
                ops.pop(); //removes (
            }else if ("+-/*".indexOf(c) != -1)
            {
                while(!ops.empty() && precedence(ops.peek()) >= precedence(c))
                {
                    if(values.size() < 2) return null;
                    Integer result = resolveNext(ops.pop(), values.pop(), values.pop());
                    if (result == null) return null;
                    values.push(result);
                }
                ops.push(c);
            }

        }

        while(!ops.empty())
        {
            if(values.size() < 2) return null;
            Integer result = resolveNext(ops.pop(), values.pop(), values.pop());
            if (result == null) return null;
            values.push(result);
        }

        return values.pop();
    }

    private static int precedence(char op){
        if(op == '+' || op == '-') return 1;
        if(op == '/' || op == '*') return 2;
        return 0;
    }

    public static Integer resolveNext(char op, int b, int a)
    {
        switch(op)
        {
            case '*':
                return a * b;
            case '/':
                if (b == 0) return null;
                if (a % b != 0) return null;
                return a/b;
            case '+':
                return a+b;
            case '-':
                return a-b;
        }
        return null;
    }

}