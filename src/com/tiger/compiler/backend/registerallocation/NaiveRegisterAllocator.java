package com.tiger.compiler.backend.registerallocation;

import com.tiger.compiler.Output;

import java.util.ArrayList;

public class NaiveRegisterAllocator extends RegisterAllocator
{
    public NaiveRegisterAllocator(String[] irCode)
    {
        super(irCode);
    }

    @Override
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
                        if (pieces.length == 3)
                        {
                            newIr.add(tabString + "load_var $t0 " + pieces[2]);
                            newIr.add(tabString + "assign $t1 $t0");
                            newIr.add(tabString + "store_var " + pieces[1] + " $t1");
                        }
                        else if (pieces.length == 4)
                        {
                            Output.println("Internal compiler error. Malformed 'assign' IR statement or array assign outside in non-initialization.");
                            System.exit(-1);
                        }
                    } break;

                    case "add":
                    case "sub":
                    case "mult":
                    case "div":
                    case "and":
                    case "or":
                    {
                        newIr.add(tabString + "load_var $t0 " + pieces[1]);
                        newIr.add(tabString + "load_var $t1 " + pieces[2]);
                        newIr.add(tabString + pieces[0] + " $t0 $t1 $t2");
                        newIr.add(tabString + "store_var " + pieces[3] + " $t2");
                    } break;

                    case "goto":
                    {
                        newIr.add(tabString + instruction);
                    } break;

                    case "return":
                    {
                        if(pieces.length == 2)
                        {
                            newIr.add(tabString + "load_var $v0 " + pieces[1]);
                            newIr.add(tabString + "return $v0");
                        }
                        else
                        {
                            newIr.add(tabString + "return");
                        }
                    } break;


                    case "call":
                    case "callr":
                    {
                        int offset = pieces[0].equals("call") ? 0 : 1;

                        int arg = 0;
                        for(int i = 2 + offset; i < pieces.length; i++)
                        {
//                            newIr.add(tabString + "load_var $a" + arg + " __" + pieces[1 + offset] + "_arg" + arg);

                            if(arg == 3)
                                break;

                            arg++;
                        }

                        newIr.add(tabString + instruction);
                    } break;

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
                    } break;

                    case "array_store":
                    {
                        newIr.add(tabString + "load_var $t0 " + pieces[2]);
                        newIr.add(tabString + "load_var $t1 " + pieces[3]);
                        newIr.add(tabString + "array_store " + pieces[1] + " $t0 $t1");
                    } break;

                    case "array_load":
                    {
                        newIr.add(tabString + "load_var $t0, " + pieces[3]);
                        newIr.add(tabString + "array_load $t1 " + pieces[2] + " $t0");
                        newIr.add(tabString + "store_var " + pieces[1] + " $t1");
                    } break;

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
}
