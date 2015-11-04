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

    public TypeSymbol getType()
    {
        return type;
    }

    public String toString()
    {
        String str = ("VariableSymbol");
        str += "\nName: " + getName();
        str += "\nType: " + type.getName();

        return str;
    }
}
