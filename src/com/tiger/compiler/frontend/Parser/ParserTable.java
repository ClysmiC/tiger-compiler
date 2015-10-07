package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.Token;

import java.util.Map;

public class ParserTable
{
    private Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table;

    public ParserTable(Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table)
    {
        this.table = table;
    }

    public ParserProduction lookup(Enum symbol1, Enum symbol2)
    {
        Tuple key = new Tuple((NonterminalSymbol)symbol1, (Token)symbol2);
        ParserProduction value = table.get(key);

        if(value == null)
            return ParserProduction.ERROR;

        return value;
    }
}
