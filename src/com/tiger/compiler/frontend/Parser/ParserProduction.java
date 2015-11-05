package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.frontend.GrammarSymbol;

import java.util.List;

public class ParserProduction
{
    private int id;
    private NonterminalSymbol lhs;
    private List<GrammarSymbol> rhs; //list of tokens and / or nonterminal symbols

    public static final ParserProduction ERROR = new ParserProduction(-1, null, null);

    public ParserProduction(int id, NonterminalSymbol lhs, List<GrammarSymbol> rhs)
    {
        this.id = id;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public int getId() {
        return id;
    }

    public NonterminalSymbol getLhs() {
        return lhs;
    }

    public List<GrammarSymbol> getRhs() {
        return rhs;
    }

    @Override
    public String toString()
    {
        String str = ("Id: " + id + " | " + lhs + " ->");

        for(GrammarSymbol e: rhs)
        {
            str += " " + e;
        }

        return str;
    }
}
