package com.tiger.compiler;

/**
 * Created by Andrew on 10/7/2015.
 */
public class Output
{
    public static boolean debugMode = false;

    private Output(){}

    public static void print(String str)
    {
        System.out.print(str);
    }

    public static void println(String str)
    {
        System.out.println(str);
    }

    public static void debugPrint(String str)
    {
        if(debugMode)
            System.out.print(str);
    }

    public static void debugPrintln(String str)
    {
        if(debugMode)
            System.out.println(str);
    }
}
