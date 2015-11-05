package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParserTable
{
    private Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table;

    public ParserTable(Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table)
    {
        this.table = table;
    }

    public ParserProduction lookup(GrammarSymbol symbol1, GrammarSymbol symbol2)
    {
        Tuple key = new Tuple((NonterminalSymbol)symbol1, (Token)symbol2);
        ParserProduction value = table.get(key);

        if(value == null)
            return ParserProduction.ERROR;

        return value;
    }

    /**
     * Returns a list of all tokens that map to legal productions for a given focus.
     *
     * @param focus
     * @return
     */
    public List<Token> getExpectedTokens(Enum focus)
    {
        List<Token> expected = new ArrayList<Token>();

        if(focus instanceof Token)
        {
            expected.add((Token)focus);
            return expected;
        }

        //test hashing into the table with every token. build a list of which ones are valid
        for(Token token: Token.values())
        {
            Tuple<NonterminalSymbol, Token> tuple = new Tuple(focus, token);

            ParserProduction result = table.get(tuple);

            if(result != null && result != ParserProduction.ERROR)
                expected.add(token);
        }

        return expected;
    }
}
