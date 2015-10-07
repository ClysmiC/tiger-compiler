package com.tiger.compiler;

import java.util.Map;

/**
 * Created by Andrew on 10/6/2015.
 */
public class ParserTable {
    private Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table;

    public ParserTable(Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table)
    {
        this.table = table;
    }

    public ParserProduction lookup(NonterminalSymbol symbol1, Token symbol2)
    {
        Tuple key = new Tuple(symbol1, symbol2);
        return table.get(key);
    }
}
