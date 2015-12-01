package com.tiger.compiler.backend.registerallocation;

import java.util.ArrayList;
import java.util.List;

public abstract class RegisterAllocator
{
    protected String[] oldIr;
    protected List<String> newIr;
    protected int nextLineNumber;

    public abstract String[] insertAllocationStatements();

    protected RegisterAllocator(String[] irCode)
    {
        this.oldIr = irCode;
        newIr = new ArrayList<>();
        nextLineNumber = 0;
    }

    protected String nextCodeLine()
    {
        while(nextLineNumber < oldIr.length)
        {
            String nextLine = oldIr[nextLineNumber];
            nextLineNumber++;

            if(nextLine.trim().isEmpty() || nextLine.trim().startsWith("#"))
            {
                newIr.add(nextLine);
                continue;
            }

            return nextLine;
        }

        //no lines left
        return null;
    }

    protected void resetCodeStream()
    {
        nextLineNumber = 0;
    }
}
