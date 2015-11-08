package com.tiger.compiler;

import com.tiger.compiler.frontend.irgeneration.TigerIrGenerator;
import com.tiger.compiler.frontend.parser.TigerParser;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.scanner.TigerScanner;
import com.tiger.compiler.frontend.semanticanalysis.TigerSemanticAnalyzer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 10/7/2015.
 */
public class TigerCompiler
{
    public static void main(String[] args)
    {
        boolean valid = true;

        if (args.length < 1)
            valid = false;

        List<String> validOptions = Arrays.asList("-t", "-pt", "-st", "-ir", "-irc", "-all", "-allc", "-o");

        for (int i = 1; i < args.length; i++)
        {
            String arg = args[i];

            if(!validOptions.contains(arg))
            {
                valid = false;
                break;
            }

            validOptions.remove(arg);

            switch(arg)
            {
                case "-t":
                    Output.printTokens = true;
                    break;
                case "-pt":
                    Output.printParseTree = true;
                    break;
                case "-st":
                    Output.printSymbolTable = true;
                    break;
                case "-ir":
                    Output.printIr = true;
                    validOptions.remove("-irc");
                    validOptions.remove("-allc");
                    break;
                case "-irc":
                    Output.printIr = true;
                    Output.printIrComments = true;
                    validOptions.remove("-ir");
                    validOptions.remove("-all");
                    break;
                case "-all":
                    Output.printTokens = true;
                    Output.printParseTree = true;
                    Output.printSymbolTable = true;
                    Output.printIr = true;
                    validOptions.remove("-irc");
                    validOptions.remove("-allc");
                    break;
                case "-allc":
                    Output.printTokens = true;
                    Output.printParseTree = true;
                    Output.printSymbolTable = true;
                    Output.printIr = true;
                    Output.printIrComments = true;
                    validOptions.remove("-ir");
                    validOptions.remove("-all");
                    break;
                case "-o":
                    Output.printToFile = true;
                    break;
                default:
                    valid = false;
            }
        }

        if(!valid)
        {
            System.out.println("Usage: java TigerCompiler <input-file>.tiger [options]");
            System.out.println("Flags:");
            System.out.println("\t-t : print tokens as they are scanned.");
            System.out.println("\t-pt : print the parse tree after successful parse.");
            System.out.println("\t-st : print the symbol table after successful parse.");
            System.out.println("\t-ir : print the IR code. May not be used with -irc flag.");
            System.out.println("\t-irc : print the IR code with comments to make it more human readable.\nMay not be used with -ir flag.");
            System.out.println("\t-all : shorthand for -t -p -st -ir. May not be used with -irc or -allc flags.");
            System.out.println("\t-allc : shorthand for -t -p -st -irc. May not be used with -ir or -all flags.");
            System.out.println("\t-o : output to file named \"<input-file>.out\"");
            System.exit(0);
        }

        TigerScanner scanner = new TigerScanner(args[0]);
        TigerParser parser = new TigerParser(scanner);

        parser.parse();

        ParseTreeNode parseTreeRoot = parser.getParseTree();
        Map<String, Symbol> globalSymbolTable = parser.getGlobalSymbolTable();

        //can't really walk an invalid parse tree. error was reported already, so just exit
        if(parseTreeRoot == null)
            System.exit(0);

        TigerSemanticAnalyzer semanticAnalyzer = new TigerSemanticAnalyzer(parseTreeRoot, globalSymbolTable);
        String[] errors = semanticAnalyzer.analyze();

        //PRINT SYMBOL TABLE
        for(String symbolId: globalSymbolTable.keySet())
        {
            Output.symbolTablePrintln(globalSymbolTable.get(symbolId) + "\n");
        }

        //PRINT PARSE TREE
        Output.parseTreePrintln(parseTreeRoot.nodeToString(0));

        if(errors.length == 0)
        {
            TigerIrGenerator ir = new TigerIrGenerator(parseTreeRoot, semanticAnalyzer.getParseTreeAttributes(), globalSymbolTable);
            String[] code = ir.generateCode();

            //PRINT IR CODE
            for(String codeLine: code)
            {
                //Only print comments in debug mode
                if(codeLine.trim().startsWith("#"))
                {
                    if(codeLine.length() == 1)
                        Output.irPrintln("");
                    else
                        Output.irPrintln(codeLine);
                }
                else
                {
                    Output.println(codeLine);
                }
            }
        }
        else
        {
            for (String error : errors)
            {
                Output.println(error + "\n");
            }
        }
    }
}
