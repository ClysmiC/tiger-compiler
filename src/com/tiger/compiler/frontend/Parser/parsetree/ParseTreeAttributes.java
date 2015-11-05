package com.tiger.compiler.frontend.parser.parsetree;

import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.TypeSymbol;

import java.util.Map;

public class ParseTreeAttributes
{
    private Map<String, Symbol> globalSymbolTable;
    private Map<String, Symbol> functionSymbolTable;
    private TypeSymbol type;

    public ParseTreeAttributes(Map<String, Symbol> globalSymbolTable, Map<String, Symbol> functionSymbolTable, TypeSymbol type)
    {
        this.globalSymbolTable = globalSymbolTable;
        this.functionSymbolTable = functionSymbolTable;
        this.type = type;
    }

    public Map<String, Symbol> getGlobalSymbolTable()
    {
        return globalSymbolTable;
    }

    public void setGlobalSymbolTable(Map<String, Symbol> globalSymbolTable)
    {
        this.globalSymbolTable = globalSymbolTable;
    }

    public Map<String, Symbol> getFunctionSymbolTable()
    {
        return functionSymbolTable;
    }

    public void setFunctionSymbolTable(Map<String, Symbol> functionSymbolTable)
    {
        this.functionSymbolTable = functionSymbolTable;
    }

    public TypeSymbol getType()
    {
        return type;
    }

    public void setType(TypeSymbol type)
    {
        this.type = type;
    }
}
