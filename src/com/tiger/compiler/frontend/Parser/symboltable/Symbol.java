package com.tiger.compiler.frontend.parser.symboltable;

/**
 * Created by Andrew on 10/27/2015.
 */
public abstract class Symbol
{
    private String name;

    public Symbol(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
