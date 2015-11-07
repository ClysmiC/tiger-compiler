package com.tiger.compiler;

import com.tiger.compiler.frontend.parser.TigerParser;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.scanner.TigerScanner;
import com.tiger.compiler.frontend.semanticanalysis.TigerSemanticAnalyzer;

import java.util.Map;

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

        ParseTreeNode parseTreeRoot = parser.getParseTree();
        Map<String, Symbol> globalSymbolTable = parser.getGlobalSymbolTable();

        TigerSemanticAnalyzer semanticAnalyzer = new TigerSemanticAnalyzer(parseTreeRoot, globalSymbolTable);
        String[] errors = semanticAnalyzer.analyze();

        Output.debugPrintln("***********SYMBOL TABLE***********");
        //print out the entire symbol table (for testing)
        for(String symbolId: globalSymbolTable.keySet())
        {
            Output.debugPrintln(globalSymbolTable.get(symbolId) + "\n");
        }

        Output.debugPrintln("\n\n\n***********PARSE TREE***********");
        Output.debugPrintln(parseTreeRoot.nodeToString(0));

        Output.debugPrintln("\n\n\n***********SEMANTIC ERRORS***********");
        for(String error: errors)
        {
           Output.println(error + "\n");
        }
    }
}
