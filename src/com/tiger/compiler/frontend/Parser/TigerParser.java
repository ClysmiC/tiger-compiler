package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Output;
import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.scanner.TigerScanner;
import com.tiger.compiler.frontend.Token;

import java.util.*;

public class TigerParser
{
    private TigerScanner tigerScanner;
    private ParserTable parserTable;

    public TigerParser(TigerScanner scanner)
    {
        tigerScanner = scanner;
        parserTable = ParserTableGenerator.generateParserTable();
    }

    public void parse()
    {
        Enum focus;
        Enum lookAhead;

        Stack<Enum> stack = new Stack<>();
        stack.add(Token.EOF);
        stack.add(NonterminalSymbol.TIGER_PROGRAM);

        focus = stack.peek();

        Tuple<Token, String> token;

        token = tigerScanner.nextToken();
        Output.debugPrintln(token.x.toString());

        lookAhead = token.x;

        while (true)
        {
            if (focus == Token.EOF && lookAhead == Token.EOF)
            {
                Output.println(!tigerScanner.isErrorRaised() ? "\nSuccessful parse" : "\nUnsuccessful parse");
                System.exit(0);
            }
            else if (focus instanceof Token)
            {
                if (focus == lookAhead)
                {
                    stack.pop();
                    token = tigerScanner.nextToken();
                    lookAhead = token.x;

                    Output.debugPrintln(token.x.toString());
                }
                else
                {
                    List<Token> expectedTokens = parserTable.getExpectedTokens(focus);

                    String errorString = "(Parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;
                    errorString += "\nExpected token: " + focus;

                    stopParsingAndExit(errorString);
                }
            }
            else
            {
                ParserProduction prod = parserTable.lookup(focus, lookAhead);

                if (prod == ParserProduction.ERROR)
                {
                    List<Token> expectedTokens = parserTable.getExpectedTokens(focus);

                    String errorString = "(Parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;

                    /**
                     * This way of getting expected tokens isn't working... look into
                     */
//                    errorString += "\nExpected token(s): ";
//
//                    for(Token t: expectedTokens)
//                    {
//                        errorString += t + ", ";
//                    }
//
//                    if(errorString.endsWith(", "))
//                    {
//                        errorString = errorString.substring(0, errorString.length() - 2);
//                    }

                    stopParsingAndExit(errorString);
                }

//                Output.debugPrintln("\nFocus: " + focus + " Lookahead: " + lookAhead);
//                Output.debugPrintln("Expanding focus: " + prod + "\n");

                stack.pop();

                List<Enum> rhs = prod.getRhs();

                for (int i = rhs.size() - 1; i >= 0; i--)
                {
                    if(rhs.get(i) != Token.NULL)
                        stack.push(rhs.get(i));
                }
            }

            focus = stack.peek();
        }
    }

    private void stopParsingAndExit(String debugMessage)
    {
        if(!debugMessage.isEmpty())
            Output.println("\n" + debugMessage + "\n");

        //print the rest of the scan. useful for detecting multiple scanner errors
        while(true)
        {

            Tuple<Token, String> token = tigerScanner.nextToken();

            //print token type
            if(token.x != Token.ERROR)
            {
                Output.debugPrintln(token.x.toString());
            }
            else
            {
                Output.println("\n" + token.y + "\n"); //error message
            }

            if(token.x == Token.EOF)
                break;
        }

        Output.println("\nUnsuccessful parse");

        System.exit(1);
    }
}
