package com.tiger.compiler.frontend.parser.symboltable;

/**
 * Entry into the Symbol Table
 */
public class TypeSymbol extends Symbol
{
    private TypeSymbol derivedType;
    private boolean isArrayOfDerivedType;
    private int arraySize;

    //types built into the language
    public final static TypeSymbol INT = new TypeSymbol("int", null);
    public final static TypeSymbol FLOAT = new TypeSymbol("float", null);

    public TypeSymbol(String name, TypeSymbol derivedType)
    {
        super(name);
        this.derivedType = derivedType;
        this.isArrayOfDerivedType = false;
        this.arraySize = 0;
    }

    public TypeSymbol(String name, TypeSymbol derivedType, int arraySize)
    {
        super(name);
        this.derivedType = derivedType;
        this.isArrayOfDerivedType = true;
        this.arraySize = arraySize;
    }

    public TypeSymbol derivedType()
    {
        return derivedType;
    }

    public boolean isArrayOfDerivedType()
    {
        return isArrayOfDerivedType;
    }

    //all types ultimately reslove to int or float
    public TypeSymbol baseType()
    {
        if(this == TypeSymbol.INT || this == TypeSymbol.FLOAT)
            return this;

        return derivedType.baseType();
    }

    public int getArraySize()
    {
        return arraySize;
    }

    public String toString()
    {
        String str = "TypeSymbol";
        str += "\nName: " + getName();

        if(this == INT || this == FLOAT)
        {
            str += "\nBase Type: n/a";
            str += "\n[Note]: primitive type added to symbol table by compiler.";
            return str;
        }

        str += "\nBase Type: " + ((isArrayOfDerivedType) ? "array[" + arraySize + "] of " : "") + derivedType.getName();
        return str;
    }
}
