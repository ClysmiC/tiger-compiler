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

        //print token type
        if(token.x != Token.ERROR)
        {
            Output.println(token.x.toString());
        }
        else
        {
            Output.println(token.y); //error message
        }

        lookAhead = token.x;

        while (true)
        {
            if (focus == Token.EOF && lookAhead == Token.EOF)
            {
                Output.println("\nSuccessful parse");
                System.exit(0);
            }
            else if (focus instanceof Token)
            {
                if (focus == lookAhead)
                {
                    stack.pop();
                    token = tigerScanner.nextToken();
                    lookAhead = token.x;

                    //print token type
                    if(token.x != Token.ERROR)
                    {
                        Output.println(token.x.toString());
                    }
                    else
                    {
                        Output.println("\n" + token.y + "\n"); //error message
                    }
                }
                else
                {
                    stopParsingAndExit("Error on line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<--- Expected: " + focus + "  Found: \"" + token.y + "\"");
                    //exitOnFailedParse("Error looking for symbol at top of stack. Parse failed.\nFocus: " + focus + "\nLookahead: " + lookAhead);
                }
            }
            else
            {
                ParserProduction prod = parserTable.lookup(focus, lookAhead);

                if (prod == ParserProduction.ERROR)
                {
                    exitOnFailedParse("Error expanding focus. Parse failed.\n" + "Focus: " + focus + "\nLookahead: " + lookAhead);
                }

                Output.debugPrintln("\nFocus: " + focus + " Lookahead: " + lookAhead);
                Output.debugPrintln("Expanding focus: " + prod + "\n");

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

    private void stopParsingAndExit(String debugMessage) {
        Output.println("\n" + debugMessage);

        Output.println("\nUnsuccessful parse.");

        System.exit(1);
    }

    private void exitOnFailedParse(String debugMessage)
    {

        //print the rest of the scan. useful for detecting multiple scanner errors
        while(true)
        {
            Tuple<Token, String> token = tigerScanner.nextToken();

            //print token type
            if(token.x != Token.ERROR)
            {
                Output.println(token.x.toString());
            }
            else
            {
                Output.println("\n" + token.y + "\n"); //error message
            }

            if(token.x == Token.EOF)
                break;
        }

        Output.debugPrintln("\n" + debugMessage);

        Output.println("\nUnsuccessful parse.");

        System.exit(1);
    }
}
