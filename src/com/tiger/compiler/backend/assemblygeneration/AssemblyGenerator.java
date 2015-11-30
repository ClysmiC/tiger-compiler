package com.tiger.compiler.backend.assemblygeneration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AssemblyGenerator
{
    private String[] ir;
    private PrintWriter out;

    private boolean error;

    public AssemblyGenerator(String outputFileName, String[] ir)
    {
        error = false;
        this.ir = ir;

        try
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
        }
        catch(Exception e)
        {
            error = true;
        }
    }

    public boolean produceAssembly()
    {
        if(error)
            return false;

        //TODO: iterate through ir and create assembly file

        return true;
    }
}
