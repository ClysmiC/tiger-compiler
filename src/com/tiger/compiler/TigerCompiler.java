package com.tiger.compiler;

import com.tiger.compiler.frontend.parser.TigerParser;
import com.tiger.compiler.frontend.scanner.TigerScanner;

/**
 * Created by Andrew on 10/7/2015.
 */
public class TigerCompiler
{
    public static void main(String[] args)
    {
        if((args.length != 1 && args.length != 2) || (args.length == 2 && !args[1].equals("-d")))
        {
            Output.print("Usage: java TigerCompiler <input-file> [-d]");
            System.exit(0);
        }

        if(args.length == 2 && args[1].equals("-d"))
        {
            Output.debugMode = true;
        }

        TigerScanner scanner = new TigerScanner(args[0]);
        TigerParser parser = new TigerParser(scanner);

        parser.parse();
    }
}
