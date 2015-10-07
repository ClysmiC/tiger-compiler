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
        if(args.length == 0)
        {
            System.out.println("Usage: java TigerCompiler <input-file>");
            System.exit(0);
        }

        TigerScanner scanner = new TigerScanner(args[0]);
        TigerParser parser = new TigerParser(scanner);

        parser.parse();
    }
}
