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
        Tuple<NonterminalSymbol, Token> key = new Tuple<>((NonterminalSymbol)symbol1, (Token)symbol2);
        ParserProduction value = table.get(key);

        if(value == null)
            return ParserProduction.ERROR;

        return value;
    }
}
