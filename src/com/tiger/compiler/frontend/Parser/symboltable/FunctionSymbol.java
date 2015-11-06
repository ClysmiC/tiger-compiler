package com.tiger.compiler.frontend.parser.symboltable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 10/27/2015.
 */
public class FunctionSymbol extends Symbol
{
    private TypeSymbol returnType;
    private List<TypeSymbol> parameterList;

    private Map<String, Symbol> functionSymbolTable; //maps variable names to their VariableSymbol

    public FunctionSymbol(String name, TypeSymbol returnType, List<TypeSymbol> parameterList)
    {
        super(name);
        this.returnType = returnType;
        this.parameterList = parameterList;

        functionSymbolTable = new HashMap<>();
    }

    public Map<String, Symbol> getSymbolTable()
    {
        return functionSymbolTable;
    }

    public String toString()
    {
        String str = "==============================";
        str += "\nFunctionSymbol";
        str += "\nName: " + getName();
        str += "\nReturns: " + ((returnType == null) ? "[void]" : returnType.getName());
        str += "\nParams: (";

        for(TypeSymbol argumentType: parameterList)
        {
            str += argumentType.getName() + ", ";
        }

        if(str.endsWith(", "))
            str = str.substring(0, str.length() - 2);

        str += ")";

        str += "\n\n***Function Symbol Table***\n";

        for(String key: functionSymbolTable.keySet())
        {
            str += "\n" + functionSymbolTable.get(key) + "\n";
        }

        str += "\n==============================";

        return str;
    }

    public TypeSymbol getReturnType()
    {
        return returnType;
    }

    public List<TypeSymbol> getParameterList()
    {
        return parameterList;
    }
}
