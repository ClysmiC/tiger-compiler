package com.tiger.compiler.frontend.parser.symboltable;

import java.util.List;

/**
 * Entry into the Symbol Table
 */
public class TypeSymbol extends Symbol
{
    private TypeSymbol baseType;
    private boolean isArrayOfBaseType;
    private int arraySize;

    //types built into the language
    public final static TypeSymbol INT = new TypeSymbol("int", null);
    public final static TypeSymbol FLOAT = new TypeSymbol("float", null);

    public TypeSymbol(String name, TypeSymbol baseType)
    {
        super(name);
        this.baseType = baseType;
        this.isArrayOfBaseType = false;
        this.arraySize = 0;
    }

    public TypeSymbol(String name, TypeSymbol baseType, int arraySize)
    {
        super(name);
        this.baseType = baseType;
        this.isArrayOfBaseType = true;
        this.arraySize = arraySize;
    }

    public TypeSymbol getBaseType()
    {
        return baseType;
    }

    public boolean isArrayOfBaseType()
    {
        return isArrayOfBaseType;
    }
}
