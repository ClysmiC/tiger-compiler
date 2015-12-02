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

        CfgNode node;

        while((node = nextBlock()) != null)
        {
            //calculate uses for each variable
            Map<String, Integer> variableUses = new HashMap<>();

            for(int i = node.getStartLine(); i <= node.getEndLine(); i++)
            {
                String instruction = oldIr[i];

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

                instruction = instruction.replaceAll(",", "");
                instruction = instruction.trim();

                if(instruction.isEmpty() || instruction.startsWith("#"))
                {
                    newIr.add(tabString + instruction);
                    continue;
                }

                String[] pieces = instruction.split(" ");

                switch(pieces[0])
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
                    } break;

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
                    } break;

                    case "goto":
                    {
                    } break;

                    case "return":
                    {
                    } break;


                    case "call":
                    case "callr":
                    {
                        int offset = pieces[0].equals("callr") ? 1 : 0;

                        for(int j = 2 + offset; j < pieces.length; j++)
                            incrementUse(variableUses, pieces[j]);

                    } break;

                    case "breq":
                    case "brneq":
                    case "brlt":
                    case "brgt":
                    case "brgeq":
                    case "brleq":
                    {
                        incrementUse(variableUses, pieces[1]);
                        incrementUse(variableUses, pieces[2]);
                    } break;

                    case "array_store":
                    {
                        //too complicated, screw it
                    } break;

                    case "array_load":
                    {
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

            List<String> variables = new ArrayList<>(variableUses.keySet());
            Collections.sort(variables, (v1, v2) -> variableUses.get(v2) - variableUses.get(v1));

            int debug = 0;
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

            newIr.add(oldIr[lineNumber]);
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
