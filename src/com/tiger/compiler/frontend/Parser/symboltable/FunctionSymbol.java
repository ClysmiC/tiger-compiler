package com.tiger.compiler.frontend.parser.symboltable;

import java.util.List;

/**
 * Created by Andrew on 10/27/2015.
 */
public class FunctionSymbol extends Symbol
{
    private TypeSymbol returnType;
    private List<TypeSymbol> argumentList;

    public FunctionSymbol(String name, TypeSymbol returnType, List<TypeSymbol> argumentList)
    {
        super(name);
        this.returnType = returnType;
        this.argumentList = argumentList;
    }
}
