package com.tiger.compiler.frontend.parser.symboltable;

/**
 * Created by Andrew on 10/27/2015.
 */
public class VariableSymbol extends Symbol
{
    private TypeSymbol type;
    private boolean initialized;
    private boolean isArray;
    private int arraySize;

    public VariableSymbol(String name, TypeSymbol type, boolean initialized, boolean isArray, int arraySize)
    {
        super(name);
        this.type = type;
        this.initialized = initialized;
        this.isArray = isArray;
        this.arraySize = arraySize;
    }

    public TypeSymbol getType()
    {
        return type;
    }

    public boolean isInitialized()
    {
        return initialized;
    }
}
