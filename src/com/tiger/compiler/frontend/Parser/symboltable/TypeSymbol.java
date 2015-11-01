package com.tiger.compiler.frontend.parser.symboltable;

import java.util.List;

/**
 * Entry into the Symbol Table
 */
public class TypeSymbol extends Symbol
{
    private TypeSymbol baseType;
    private boolean isArrayOfBaseType;

    //types built into the language
    public final static TypeSymbol INT = new TypeSymbol("int", null, false);
    public final static TypeSymbol FLOAT = new TypeSymbol("float", null, false);

    public TypeSymbol(String name, TypeSymbol baseType, boolean isArray)
    {
        super(name);
        this.baseType = baseType;
        this.isArrayOfBaseType = isArray;
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
