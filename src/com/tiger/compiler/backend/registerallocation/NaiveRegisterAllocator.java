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
        boolean pastFuncInitialization = false;

        String instruction;
        while((instruction = nextCodeLine()) != null)
        {
            String regPrefix = (pastFuncInitialization) ? "s" : "t";

            if(!pastVarInitialization)
            {
                instruction = instruction.replaceAll(",", "");
                newIr.add(instruction);

                if (instruction.trim().startsWith("goto")) //goto program_start
                    pastVarInitialization = true;
            }
            else
            {
                if(instruction.trim().equals("_program_start:"))
                    pastFuncInitialization = true;

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
                            instruction(tabString + "load_var $%0 " + pieces[2], regPrefix);
                            instruction(tabString + "assign $%1 $%0", regPrefix);
                            instruction(tabString + "store_var " + pieces[1] + " $%1", regPrefix);
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
                        instruction(tabString + "load_var $%0 " + pieces[1], regPrefix);
                        instruction(tabString + "load_var $%1 " + pieces[2], regPrefix);
                        instruction(tabString + pieces[0] + " $%0 $%1 $%2", regPrefix);
                        instruction(tabString + "store_var " + pieces[3] + " $%2", regPrefix);
                    } break;

                    case "goto":
                    {
                        newIr.add(tabString + instruction);
                    } break;

                    case "return":
                    {
                        if(pieces.length == 2)
                        {
                            instruction(tabString + "load_var $v0 " + pieces[1], regPrefix);
                            instruction(tabString + "return $v0", regPrefix);
                        }
                        else
                        {
                            instruction(tabString + "return", regPrefix);
                        }
                    } break;


                    case "call":
                    case "callr":
                    {
                        int offset = pieces[0].equals("call") ? 0 : 1;

                        int arg = 0;
                        for(int i = 2 + offset; i < pieces.length; i++)
                        {
//                            instruction(tabString + "load_var $a" + arg + " __" + pieces[1 + offset] + "_arg" + arg, regPrefix);

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
                        instruction(tabString + "load_var $%0 " + pieces[1], regPrefix);
                        instruction(tabString + "load_var $%1 " + pieces[2], regPrefix);
                        instruction(tabString + pieces[0] + " $%0 $%1 " + pieces[3], regPrefix);
                    } break;

                    case "array_store":
                    {
                        instruction(tabString + "load_var $%0 " + pieces[2], regPrefix);
                        instruction(tabString + "load_var $%1 " + pieces[3], regPrefix);
                        instruction(tabString + "array_store " + pieces[1] + " $%0 $%1", regPrefix);
                    } break;

                    case "array_load":
                    {
                        instruction(tabString + "load_var $%0, " + pieces[3], regPrefix);
                        instruction(tabString + "array_load $%1 " + pieces[2] + " $%0", regPrefix);
                        instruction(tabString + "store_var " + pieces[1] + " $%1", regPrefix);
                    } break;

                    default:
                    {
                        //labels
                        if (instruction.contains(":"))
                        {
                            instruction(tabString + instruction, regPrefix);
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

    private void instruction(String instruction, String registerPrefix)
    {
        newIr.add(instruction.replaceAll("\\%", registerPrefix));
    }
}
