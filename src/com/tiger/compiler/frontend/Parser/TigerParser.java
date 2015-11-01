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

    //Hold onto certain values so that if a semantic action is required (such as putting
    //something in a symbol table), we have all the info we need.
    private String latestId;
    private List<String> idList; //used for declaring variables in list fashion. e.g. var x, y, z : int := 5

    public TigerParser(TigerScanner scanner)
    {
        tigerScanner = scanner;
        parserTable = ParserTableGenerator.generateParserTable();

        idList = new LinkedList<>();
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
                    if(focus == Token.ID)
                    {
                        latestId = token.y;
                    }

                    stack.pop();
                    token = tigerScanner.nextToken();
                    lookAhead = token.x;

                    Output.debugPrintln(token.x.toString());
                }
                else
                {
                    String errorString = "(parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;
                    errorString += "\nExpected token: " + focus;

                    stopParsingAndExit(errorString);
                }
            }
            else if (focus instanceof NonterminalSymbol)
            {
                ParserProduction prod = parserTable.lookup(focus, lookAhead);

                if (prod == ParserProduction.ERROR)
                {
                    String errorString = "(parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;

                    //TODO: suggest tokens
                    //TODO: recover from error and continue parse

                    stopParsingAndExit(errorString);
                }

                stack.pop();

                List<Enum> rhs = prod.getRhs();

                for (int i = rhs.size() - 1; i >= 0; i--)
                {
                    if(rhs.get(i) != Token.NULL)
                        stack.push(rhs.get(i));
                }
            }
            else if(focus instanceof SemanticAction)
            {
                SemanticAction action = (SemanticAction)focus;

                switch(action)
                {
                    case PUT_TYPE:
                    {
                        //TODO: code for this case....
                    } break;
                    //TODO: add a case for each semantic action
                }

                stack.pop();
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
