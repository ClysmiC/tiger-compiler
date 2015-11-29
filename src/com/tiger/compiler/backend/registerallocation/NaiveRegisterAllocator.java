package com.tiger.compiler.backend.registerallocation;

import com.tiger.compiler.Output;

import java.util.ArrayList;
import java.util.List;

public class NaiveRegisterAllocator
{
    private List<String> oldIr;
    private List<String> newIr;
    private int nextLineNumber;

    public NaiveRegisterAllocator(List<String> irCode)
    {
        this.oldIr = irCode;
        newIr = new ArrayList<>();
        nextLineNumber = 0;
    }

    public List<String> insertAllocationStatements()
    {
        String instruction;
        while((instruction = nextCodeLine()) != null)
        {
            String tabString = ""; //keep new code formatted nicely :)
            for(int i = 0; i < instruction.length(); i++)
            {
                if(instruction.charAt(i) == ' ')
                    tabString += " ";
                else
                    break;
            }

            instruction = instruction.replaceAll(",", "");
            String[] pieces = instruction.split(" ");

            switch(pieces[0])
            {
                case "assign":
                {
                    //non-array
                    //TODO: handle array assignment
                    newIr.add(tabString + "load_var, $t0, " + pieces[2] + ",");
                    newIr.add(tabString + "assign, $t1, $t0,");
                    newIr.add(tabString + "store_var, " + pieces[1] + ", $t1,");
                } break;

                case "add":
                case "sub":
                case "mult":
                case "div":
                case "and":
                case "or":
                {
                    newIr.add(tabString + "load_var, $t0, " + pieces[1] + ",");
                    newIr.add(tabString + "load_var, $t1, " + pieces[2] + ",");
                    newIr.add(tabString + pieces[0] + ", t0, t1, t2");
                    newIr.add(tabString + "store_var, " + pieces[3] + ", $t2,");
                } break;

                case "goto":
                case "call":
                case "callr":
                case "return":
                {
                    newIr.add(instruction);
                } break;

                case "breq":
                case "brneq":
                case "brlt":
                case "brgt":
                case "brgeq":
                case "brleq":
                {
                    newIr.add(tabString + "load_var, $t0, " + pieces[1] + ",");
                    newIr.add(tabString + "load_var, $t1, " + pieces[2] + ",");
                    newIr.add(tabString + pieces[0] + ", t0, t1, " + pieces[3]);
                } break;

                case "array_store":
                {
                } break;

                case "array_load":
                {
                } break;

                default:
                {
                    Output.println("Internal compiler error in NaiveRegisterAllocator.");
                    System.exit(-1);
                }
            }
        }

        return newIr;
    }

    private void resetCodeStream()
    {
        nextLineNumber = 0;
    }

    private String nextCodeLine()
    {
        while(nextLineNumber < oldIr.size())
        {
            String nextLine = oldIr.get(nextLineNumber);
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
}
