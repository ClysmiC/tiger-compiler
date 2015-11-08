package com.tiger.compiler;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by Andrew on 10/7/2015.
 */
public class Output
{
    public static boolean printTokens = false;
    public static boolean printParseTree = false;
    public static boolean printSymbolTable = false;
    public static boolean printIr = false;
    public static boolean printIrComments = false;
    public static boolean printToFile = false;

    private static PrintWriter out = null;
    private static String outputName = null;

    private Output()
    {
    }

    //Note: every function is a println (that is, prints a new line at the end).
    //It is the job of the calling class to format/concatenate any strings that
    //they want to print on a line together, before passing the string to this class
    public static void println(String str)
    {
        System.out.println(str);

        if(out != null)
            out.println(str);
    }

    public static void tokenPrintln(String str)
    {
        if (printTokens)
            println(str);
    }

    public static void parseTreePrintln(String str)
    {
        if (printParseTree)
            println(str);
    }

    public static void symbolTablePrintln(String str)
    {
        if (printSymbolTable)
            println(str);
    }

    public static void irPrintln(String str)
    {
        if(!printIr)
            return;

        //Only print comments if flag was set
        if(str.trim().startsWith("#"))
        {
            if(printIrComments)
            {
                if(str.length() == 1)
                    Output.println("");
                else
                    Output.println(str);
            }
        }
        else
        {
            Output.println(str);
        }
    }

    public static void setOutputFileName(String str)
    {
        try
        {
            outputName = (str);
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputName)));
        }
        catch(Exception e)
        {
            //couldn't open file writer... just don't output to file
            out = null;
            outputName = null;
        }
    }

    public static void closeOutputFile()
    {
        if(out != null)
            out.close();
    }

    public static String getOutputFileName()
    {
        return outputName;
    }
}
