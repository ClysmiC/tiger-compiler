package com.tiger.compiler.frontend.parser;

import java.util.List;

public class ParserProduction
{
    private int id;
    private NonterminalSymbol lhs;
    private List<Enum> rhs; //list of tokens and / or nonterminal symbols

    public static final ParserProduction ERROR = new ParserProduction(-1, null, null);

    public ParserProduction(int id, NonterminalSymbol lhs, List<Enum> rhs)
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

    public List<Enum> getRhs() {
        return rhs;
    }

    @Override
    public String toString()
    {
        String str = ("Id: " + id + " | " + lhs + " ->");

        for(Enum e: rhs)
        {
            str += " " + e;
        }

        return str;
    }
}
