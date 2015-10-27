package com.tiger.compiler.frontend.parser.symboltable;

/**
 * Created by Andrew on 10/27/2015.
 */
public class VariableSymbol extends Symbol
{
    private TypeSymbol type;

    public VariableSymbol(String name, TypeSymbol type)
    {
        super(name);
        this.type = type;
    }
}
