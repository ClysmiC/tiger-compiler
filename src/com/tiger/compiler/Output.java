package com.tiger.compiler;

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

    private Output()
    {
    }

    //Note: every function is a println (that is, prints a new line at the end).
    //It is the job of the calling class to format/concatenate any strings that
    //they want to print on a line together, before passing the string to this class
    public static void println(String str)
    {
        //TODO: print to file as well
        System.out.println(str);
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
}
