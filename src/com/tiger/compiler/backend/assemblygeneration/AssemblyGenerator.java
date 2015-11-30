package com.tiger.compiler.backend.assemblygeneration;

import com.tiger.compiler.Output;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.VariableSymbol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssemblyGenerator
{
    private String[] ir;
    private PrintWriter out;

    private List<String> allVariables;

    private boolean error;
    private Map<String, Symbol> symbolTable;

    public AssemblyGenerator(String outputFileName, String[] ir, Map<String, Symbol> symbolTable)
    {
        error = false;
        this.ir = ir;
        this.symbolTable = symbolTable;

        try
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
        }
        catch(Exception e)
        {
            error = true;
        }

        allVariables = new ArrayList<>();
        findAllVariables();
    }

    public boolean produceAssembly()
    {
        if(error)
            return false;

        out.println(".data");

        out.println("\n#User-created variables (excluding arrays)");
        for(String line: ir)
        {
            line = line.trim();

            if(line.equals("goto _program_start")){}
        }

        out.println("\n#User-created array variables");

        out.println("\n#Compiler-created variables.");



        return true;
    }

    private void findAllVariables()
    {
        for (String codeLine : ir)
        {
            String str = codeLine.trim();
            if (str.isEmpty() || str.startsWith("#") || str.contains(":"))
                continue;

            str = str.replaceAll(",", "");
            String[] pieces = str.split(" ");

            if(pieces[0].equals("store_var"))
            {
                addToVariablesList(pieces[1]);
            }
            else if(pieces[0].equals("load_var"))
            {
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
