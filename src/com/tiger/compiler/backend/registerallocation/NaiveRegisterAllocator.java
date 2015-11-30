package com.tiger.compiler.backend.registerallocation;

import com.tiger.compiler.Output;

import java.util.ArrayList;
import java.util.List;

public class NaiveRegisterAllocator
{
    private String[] oldIr;
    private List<String> newIr;
    private int nextLineNumber;

    public NaiveRegisterAllocator(String[] irCode)
    {
        this.oldIr = irCode;
        newIr = new ArrayList<>();
        nextLineNumber = 0;
    }

    public String[] insertAllocationStatements()
    {
        //all var initializations are literals,
        //so we don't need to do any load/stores
        boolean pastVarInitialization = false;

        String instruction;
        while((instruction = nextCodeLine()) != null)
        {
            if(!pastVarInitialization)
            {
                instruction = instruction.replaceAll(",", "");
                newIr.add(instruction);

                if (instruction.trim().startsWith("goto")) //goto program_start
                    pastVarInitialization = true;
            }
            else
            {
                String tabString = ""; //keep new code formatted nicely :)
                for (int i = 0; i < instruction.length(); i++)
                {
                    if (instruction.charAt(i) == ' ')
                        tabString += " ";
                    else if (instruction.charAt(i) == '\t')
                        tabString += "\t";
                    else
                        break;
                }

                instruction = instruction.replaceAll(",", "");
                instruction = instruction.trim();
                String[] pieces = instruction.split(" ");

                switch (pieces[0])
                {
                    case "assign":
                    {
                        //non-array
                        //TODO: handle array assignment
                        //note: this should only happen in variable initialization

                        if (pieces.length == 3)
                        {
                            newIr.add(tabString + "load_var $t0 " + pieces[2]);
                            newIr.add(tabString + "assign $t1 $t0");
                            newIr.add(tabString + "store_var " + pieces[1] + " $t1");
                        }
                        else if (pieces.length == 4)
                        {
                            newIr.add(tabString + "load_var $t0 " + pieces[3]);
                            newIr.add(tabString + "assign " + pieces[1] + " " + pieces[2] + " $t0");
                            newIr.add(tabString + "#Since array-assign is a special case, we will let the assembly generator handle the stores.\n");
                        }
                        else
                        {
                            Output.println("Internal compiler error. Malformed 'assign' IR statement");
                            System.exit(-1);
                        }
                    }
                    break;

                    case "add":
                    case "sub":
                    case "mult":
                    case "div":
                    case "and":
                    case "or":
                    {
                        newIr.add(tabString + "load_var $t0 " + pieces[1]);
                        newIr.add(tabString + "load_var $t1, " + pieces[2]);
                        newIr.add(tabString + pieces[0] + " $t0 $t1 $t2");
                        newIr.add(tabString + "store_var " + pieces[3] + " $t2");
                    }
                    break;

                    case "goto":
                    case "call":
                    case "callr":
                    case "return":
                    {
                        newIr.add(instruction);
                    }
                    break;

                    case "breq":
                    case "brneq":
                    case "brlt":
                    case "brgt":
                    case "brgeq":
                    case "brleq":
                    {
                        newIr.add(tabString + "load_var $t0 " + pieces[1]);
                        newIr.add(tabString + "load_var $t1 " + pieces[2]);
                        newIr.add(tabString + pieces[0] + " $t0 $t1 " + pieces[3]);
                    }
                    break;

                    case "array_store":
                    {
                        newIr.add(tabString + "load_var $t0 " + pieces[2]);
                        newIr.add(tabString + "load_var $t1 " + pieces[3]);
                        newIr.add(tabString + "array_store " + pieces[1] + " $t0 $t1");
                    }
                    break;

                    case "array_load":
                    {
                        newIr.add(tabString + "load_var $t0, " + pieces[3]);
                        newIr.add(tabString + "array_load $t1, " + pieces[2] + " $t0");
                        newIr.add(tabString + "store_var " + pieces[1] + " $t1");
                    }
                    break;

                    default:
                    {
                        //labels
                        if (instruction.contains(":"))
                        {
                            newIr.add(tabString + instruction);
                        }
                        else
                        {
                            Output.println("\n\nERROR: Internal compiler error in NaiveRegisterAllocator.");
                            System.exit(-1);
                        }
                    }
                }
            }
        }

        return newIr.toArray(new String[newIr.size()]);
    }

    private void resetCodeStream()
    {
        nextLineNumber = 0;
    }

    private String nextCodeLine()
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
}
