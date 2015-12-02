package com.tiger.compiler.backend.registerallocation;

import com.tiger.compiler.Output;
import com.tiger.compiler.backend.registerallocation.cfg.CfgBuilder;
import com.tiger.compiler.backend.registerallocation.cfg.CfgNode;

import java.util.*;

public class BasicBlockAllocator extends RegisterAllocator
{
    private List<CfgNode> cfg;
    private int lineNumber;

    public BasicBlockAllocator(String[] irCode)
    {
        super(irCode);
    }

    @Override
    public String[] insertAllocationStatements()
    {
        CfgBuilder cfgBuilder = new CfgBuilder(oldIr);
        cfg = cfgBuilder.constructCfg();

        boolean pastFunctionDeclaration = false;

        CfgNode node;
        while((node = nextBlock()) != null)
        {
            //calculate uses for each variable
            Map<String, Integer> variableUses = new HashMap<>();

            for (int i = node.getStartLine(); i <= node.getEndLine(); i++)
            {
                String instruction = oldIr[i];
                instruction = instruction.replaceAll(",", "");
                instruction = instruction.trim();

                if (instruction.isEmpty() || instruction.startsWith("#"))
                {
                    continue;
                }

                String[] pieces = instruction.split(" ");

                switch (pieces[0])
                {

                    case "assign":
                    {
                        if (pieces.length == 3)
                        {
                            incrementUse(variableUses, pieces[1]);
                            incrementUse(variableUses, pieces[2]);
                        }
                        else if (pieces.length == 4)
                        {
                            Output.println("Internal compiler error. Malformed 'assign' IR statement or array assign outside in non-initialization.");
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
                        incrementUse(variableUses, pieces[1]);
                        incrementUse(variableUses, pieces[2]);
                        incrementUse(variableUses, pieces[3]);
                    }
                    break;

                    case "goto":
                    {
                    }
                    break;

                    case "return":
                    {
                    }
                    break;


                    case "call":
                    case "callr":
                    {
                        int offset = pieces[0].equals("callr") ? 1 : 0;

                        for (int j = 2 + offset; j < pieces.length; j++)
                            incrementUse(variableUses, pieces[j]);

                    }
                    break;

                    case "breq":
                    case "brneq":
                    case "brlt":
                    case "brgt":
                    case "brgeq":
                    case "brleq":
                    {
                        incrementUse(variableUses, pieces[1]);
                        incrementUse(variableUses, pieces[2]);
                    }
                    break;

                    case "array_store":
                    {
                        //too complicated, screw it
                    }
                    break;

                    case "array_load":
                    {
                    }
                    break;

                    default:
                    {
                        //labels
                        if (instruction.contains(":"))
                        {
                            if (instruction.equals("_program_start:"))
                                pastFunctionDeclaration = true;
                        }
                        else
                        {
                            Output.println("\n\nERROR: Internal compiler error in NaiveRegisterAllocator.");
                            System.exit(-1);
                        }
                    }
                }
            }

            List<String> variables = new ArrayList<>(variableUses.keySet());
            Collections.sort(variables, (v1, v2) -> variableUses.get(v2) - variableUses.get(v1));

            //map 15 most used variables to registers.
            //remaining variables will use register t0, t1, t2 as well as memory spills
            Map<String, String> varToRegister = new HashMap<>();
            for (int i = 0; i < 15; i++)
            {
                if (i >= variables.size())
                    break;

                String register;

                if(i < 8)
                    register = "$s" + i;
                else
                    register = "$t" + ((i % 8) + 3); //t3, t4, t5...


                varToRegister.put(variables.get(i), register);
            }

            //start line gets special treatment, since if the first line is a label
            //we want to print it, but if it is a statement, we want to do the loads
            //before the stmts
            if (oldIr[node.getStartLine()].contains(":"))
            {
                newIr.add(oldIr[node.getStartLine()]);
            }

            //load all variables as soon as we enter block (right after label)
            for (String variable : varToRegister.keySet())
            {
                newIr.add("\tload_var " + varToRegister.get(variable) + " " + variable);
            }

            //for something like an unconditional branch, we want to make sure we print it AFTER
            //we have put registers for the block back into memory. block ending instruction
            //may reside in this string, to be printed after the stores happen
            String delayedInstruction = "";

            //keep track of which variables were changed in register, so we know which ones we have
            //to store
            Set<String> modifiedVariables = new HashSet<>();

            for (int i = node.getStartLine(); i <= node.getEndLine(); i++)
            {
                String instruction = oldIr[i];
                instruction = instruction.replaceAll(",", "");

                String tabString = ""; //keep new code formatted nicely :)
                for (int j = 0; j < instruction.length(); j++)
                {
                    if (instruction.charAt(j) == ' ')
                        tabString += " ";
                    else if (instruction.charAt(j) == '\t')
                        tabString += "\t";
                    else
                        break;
                }

                instruction = instruction.trim();

                //only first stmt should be label, which is handled before loop
                if (instruction.contains(":"))
                    continue;

                if (instruction.isEmpty() || instruction.startsWith("#"))
                {
                    newIr.add(tabString + instruction);

                    continue;
                }

                String[] pieces = instruction.split(" ");

                switch (pieces[0])
                {
                    case "assign":
                    {
                        boolean spillResult = false;

                        String targetRegister;
                        String sourceRegister;
                        if (varToRegister.containsKey(pieces[1]))
                        {
                            targetRegister = varToRegister.get(pieces[1]);
                            modifiedVariables.add(pieces[1]);
                        }
                        else
                        {
                            targetRegister = "$t1";
                            spillResult = true;
                        }

                        if (varToRegister.containsKey(pieces[2]))
                        {
                            sourceRegister = varToRegister.get(pieces[2]);
                        }
                        else
                        {
                            newIr.add(tabString + "load_var $t0 " + pieces[2]);
                            sourceRegister = "$t0";
                        }

                        newIr.add(tabString + "assign " + targetRegister + " " + sourceRegister);

                        if (spillResult)
                        {
                            newIr.add(tabString + "store_var " + pieces[1] + " " + targetRegister);
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
                        boolean spillResult = false;

                        String targetRegister;
                        String leftRegister;
                        String rightRegister;
                        if (varToRegister.containsKey(pieces[3]))
                        {
                            targetRegister = varToRegister.get(pieces[3]);
                            modifiedVariables.add(pieces[3]);
                        }
                        else
                        {
                            targetRegister = "$t2";
                            spillResult = true;
                        }

                        if (varToRegister.containsKey(pieces[1]))
                        {
                            leftRegister = varToRegister.get(pieces[1]);
                        }
                        else
                        {
                            newIr.add(tabString + "load_var $t0 " + pieces[2]);
                            leftRegister = "$t0";
                        }

                        if (varToRegister.containsKey(pieces[2]))
                        {
                            rightRegister = varToRegister.get(pieces[2]);
                        }
                        else
                        {
                            newIr.add(tabString + "load_var $t1 " + pieces[2]);
                            rightRegister = "$t1";
                        }

                        newIr.add(tabString + pieces[0] + " " + leftRegister + " " + rightRegister + " " + targetRegister);

                        if (spillResult)
                        {
                            newIr.add(tabString + "store_var " + pieces[3] + " " + targetRegister);
                        }
                    }
                    break;

                    case "goto":
                    {
                        delayedInstruction = tabString + instruction;
                    }
                    break;

                    case "return":
                    {
                        if (pieces.length == 2)
                        {
                            String retValRegister;
                            if (varToRegister.containsKey(pieces[1]))
                            {
                                retValRegister = varToRegister.get(pieces[1]);
                            }
                            else
                            {
                                newIr.add(tabString + "load_var $t0 " + pieces[1]);
                                retValRegister = "$t0";
                            }

                            newIr.add(tabString + "assign $v0 " + retValRegister);
                            delayedInstruction = tabString + "return $v0";
                        }
                        else
                        {
                            delayedInstruction = tabString + "return";
                        }
                    }
                    break;


                    case "call":
                    case "callr":
                    {
                        int offset = pieces[0].equals("call") ? 0 : 1;

                        String call = tabString + pieces[0];

                        if(pieces[0].equals("callr"))
                            call += " " + pieces[1];


                        int arg = 0;
                        for (int j = 2 + offset; j < pieces.length; j++)
                        {

                            if (arg == 3)
                                break;

                            String argRegister;
                            if(varToRegister.containsKey(pieces[j]))
                            {
                                argRegister = varToRegister.get(pieces[j]);
//                                newIr.add(tabString + "assign $a" + arg + " " + argRegister);
                            }
                            else
                            {
//                                newIr.add(tabString + "load_var $a" + arg + " __" + pieces[1 + offset] + "_arg" + arg);
                            }

                            arg++;
                        }

                        delayedInstruction = tabString + instruction;
                    }
                    break;

                    case "breq":
                    case "brneq":
                    case "brlt":
                    case "brgt":
                    case "brgeq":
                    case "brleq":
                    {
                        String register1;
                        String register2;

                        if (varToRegister.containsKey(pieces[1]))
                        {
                            register1 = varToRegister.get(pieces[1]);
                        }
                        else
                        {
                            newIr.add(tabString + "load_var $t0 " + pieces[1]);
                            register1 = "$t0";
                        }

                        if (varToRegister.containsKey(pieces[2]))
                        {
                            register2 = varToRegister.get(pieces[2]);
                        }
                        else
                        {
                            newIr.add(tabString + "load_var $t1 " + pieces[2]);
                            register2 = "$t1";
                        }

                        newIr.add(tabString + pieces[0] + " " + register1 + " " + register2 + " " + pieces[3]);
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
                        newIr.add(tabString + "array_load $t1 " + pieces[2] + " $t0");
                        newIr.add(tabString + "store_var " + pieces[1] + " $t1");
                    }
                    break;

                    default:
                    {
                        Output.println("\n\nERROR: Internal compiler error in NaiveRegisterAllocator.");
                        System.exit(-1);
                    }
                }
                //replace variables in IR instructions with their registers
                //if they aren't within a register, do naive load, store, etc with registers 0, 1, 2
            }

            //load all variables as soon as we enter block (right after label)
            for (String variable : varToRegister.keySet())
            {
                if(modifiedVariables.contains(variable))
                    newIr.add("\tstore_var " + variable + " " + varToRegister.get(variable));
            }

            if (!delayedInstruction.isEmpty())
            {
                newIr.add(delayedInstruction);
            }

            lineNumber = node.getEndLine() + 1;
        }

        return newIr.toArray(new String[newIr.size()]);
    }

    private CfgNode nextBlock()
    {
        while(lineNumber < oldIr.length)
        {
            for (CfgNode node : cfg)
            {
                if (node.getStartLine() == lineNumber)
                {
                    lineNumber++;
                    return node;
                }
            }

            newIr.add(oldIr[lineNumber].replaceAll(",", ""));
            lineNumber++;
        }

        return null;
    }

    private void incrementUse(Map<String, Integer> uses, String variable)
    {
        if(isNumeric(variable))
            return;

        if(uses.containsKey(variable))
        {
            uses.put(variable, uses.get(variable) + 1);
        }
        else
        {
            uses.put(variable, 1);
        }
    }

    public boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}
