package com.tiger.compiler.frontend.parser;

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

        Stack<Enum> stack = new Stack();
        stack.add(Token.EOF);
        stack.add(NonterminalSymbol.TIGER_PROGRAM);

        focus = stack.peek();

        Tuple<Token, String> token;

        token = tigerScanner.nextToken();
        lookAhead = token.x;

        while (true)
        {

            if(token.x == Token.ERROR)
            {
                System.out.println("\n" + token.y + "\n");
            }
            else
            {
                if (focus == Token.EOF && lookAhead == Token.EOF)
                {
                    System.out.println("Successful parse");
                    System.exit(0);
                }
                else if (focus instanceof Token)
                {
                    if (focus == lookAhead)
                    {
                        stack.pop();
                        lookAhead = tigerScanner.nextToken().x;
                    }
                    else
                    {
                        System.out.println("\nError looking for symbol at top of stack. Parse failed.\nFocus: " + focus + "\nLookahead: " + lookAhead);
                        System.exit(1);
                    }
                }
                else
                {
                    ParserProduction prod = parserTable.lookup(focus, lookAhead);

                    if (prod == ParserProduction.ERROR)
                    {
                        System.out.println("\nError expanding focus. Parse failed.\nFocus: " + focus + "\nLookahead: " + lookAhead);
                        System.exit(1);
                    }

                    System.out.println("\nFocus: " + focus + " Lookahead: " + lookAhead);
                    System.out.println("Expanding focus: " + prod);

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
    }
}
