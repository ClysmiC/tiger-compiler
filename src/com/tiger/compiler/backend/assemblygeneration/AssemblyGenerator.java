package com.tiger.compiler.backend.assemblygeneration;

import com.tiger.compiler.Output;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssemblyGenerator
{
    private String[] ir;
    private List<String> asm;

    private List<String> allVariables;

    public AssemblyGenerator(String[] ir)
    {
        this.ir = ir;
        allVariables = new ArrayList<>();
        findAllVariables();
        asm = new ArrayList<>();
    }

    public String[] produceAssembly()
    {
        asm.add(".data");

        asm.add("\n#User-created variables");
        {
            for (String line : ir)
            {
                line = line.trim();

                if(line.isEmpty() || line.startsWith("#"))
                    continue;

                String[] pieces = line.split(" ");

                if (pieces[0].equals("goto"))
                {
                    break; //var initialization complete
                }

                if(pieces[0].equals("assign"))
                {
                    if(pieces.length == 3)
                    {
                        //ignore temporary registers in the variable initialization segment.
                        //they are a side-product of the process through which the ir-generator
                        //emits code, but they are redundant and useless in this segment for the assembly
                        if(pieces[1].startsWith("_"))
                            continue;

                        asm.add(pieces[1] + ": .word " + pieces[2]);
                    }
                    else if (pieces.length == 4)
                    {
                        asm.add(pieces[1] + ": .word " + pieces[3] + ":" + pieces[2]);
                    }
                    else
                    {
                        Output.println("Internal compiler error. Malformed 'assign' IR statement");
                        System.exit(-1);
                    }
                }
                else
                {
                    System.out.println("Internal compiler error. Non-assign IR statements before program body.");
                    System.exit(-1);
                }
            }
        }

        asm.add("\n#Compiler-created variables.");


        return asm.toArray(new String[asm.size()]);
    }

    private void findAllVariables()
    {
        for (String codeLine : ir)
        {
            String str = codeLine.trim();
            if (str.isEmpty() || str.startsWith("#") || str.contains(":"))
                continue;

            String[] pieces = str.split(" ");

            if(pieces[0].equals("store_var"))
            {
                addToVariablesList(pieces[1]);
            }
            else if(pieces[0].equals("load_var"))
            {
                if(pieces.length < 3)
                {
                    int debug = 93023;
                }

                addToVariablesList(pieces[2]);
            }
            else if(pieces[0].equals("assign") && pieces.length == 4)
            {
                //assigning to entire array (this always gets done, explicitly, or implicitly (to 0))
                addToVariablesList(pieces[1]);
            }
        }
    }

    public boolean addToVariablesList(String str)
    {
        if(str.matches("-?\\d+(\\.\\d+)?"))
            return false;
        else if(allVariables.contains(str))
            return false;

        allVariables.add(str);
        return true;
    }
}
