package com.tiger.compiler.frontend.parser.symboltable;

import java.util.List;

/**
 * Entry into the Symbol Table
 */
public class TypeSymbol extends Symbol
{

    private String baseType;

    //types built into the language
    public final static TypeSymbol INT = new TypeSymbol("int", "int");
    public final static TypeSymbol INT_ARRAY = new TypeSymbol("int_array", "int_array");
    public final static TypeSymbol FLOAT = new TypeSymbol("float", "float");
    public final static TypeSymbol FLOAT_ARRAY = new TypeSymbol("float_array", "float_array");

    public TypeSymbol(String name, String baseType)
    {
        super(name);
        this.baseType = baseType;
    }
}
