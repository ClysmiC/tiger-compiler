package com.tiger.compiler.frontend.parser.symboltable;

import java.util.List;

/**
 * Entry into the Symbol Table
 */
public class TypeSymbol extends Symbol
{
    private TypeSymbol derivedFrom;

    //types built into the language
    public final static TypeSymbol INT = new TypeSymbol("int", null);
    public final static TypeSymbol FLOAT = new TypeSymbol("float", null);
    public final static TypeSymbol INT_ARRAY = new TypeSymbol("int_array", null);
    public final static TypeSymbol FLOAT_ARRAY = new TypeSymbol("float_array", null);

    public TypeSymbol(String name, TypeSymbol derivedFrom)
    {
        super(name);
        this.derivedFrom = derivedFrom;
    }
}
