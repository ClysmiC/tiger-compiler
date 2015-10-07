package com.tiger.compiler;

import java.util.Map;

/**
 * Created by Andrew on 10/6/2015.
 */
public class TigerParser {

    public static void main(String[] args) {
        TigerScanner tigerScanner = new TigerScanner();
        ParserTable parserTable = ParserTableGenerator.generateParserTable();


        Tuple<Token, String> token;
        while (true)
        {
            token = tigerScanner.nextToken();
            System.out.println(token);

            if(token.x == Token.EOF)
            {
                /**
                 * Test parser table
                 */
                System.out.println(parserTable.lookup(NonterminalSymbol.STAT_ASSIGN_OR_FUNC, Token.SEMI));
                System.exit(0);
            }
        }
    }
}
