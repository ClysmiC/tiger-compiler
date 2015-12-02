package com.tiger.compiler;

import com.tiger.compiler.backend.assemblygeneration.AssemblyGenerator;
import com.tiger.compiler.backend.registerallocation.BasicBlockAllocator;
import com.tiger.compiler.backend.registerallocation.NaiveRegisterAllocator;
import com.tiger.compiler.backend.registerallocation.cfg.CfgBuilder;
import com.tiger.compiler.frontend.irgeneration.TigerIrGenerator;
import com.tiger.compiler.frontend.parser.TigerParser;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.scanner.TigerScanner;
import com.tiger.compiler.frontend.semanticanalysis.TigerSemanticAnalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

        if(!args[0].toLowerCase().endsWith(".tiger"))
            valid = false;

        if(args.length == 2 && args[1].equals("-o"))
            valid = false;

        List<String> validOptions = new ArrayList<>(Arrays.asList("-t", "-pt", "-st", "-ir", "-irc", "-all", "-allc", "-o", "-r1", "-r2"));

        int regAllocScheme = 2;

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
                case "-r1":
                    validOptions.remove("-r2");
                    regAllocScheme = 1;
                    break;
                case "-r2":
                    validOptions.remove("-r1");
                    regAllocScheme = 2;
                    break;
                default:
                    valid = false;
            }
        }

        if(!valid)
        {
            System.out.println("Usage: java TigerCompiler <input-file>.tiger [options]");
            System.out.println("Options:");
            System.out.println("\t-t    : Print tokens as they are scanned.");
            System.out.println("\t-pt   : Print the parse tree after successful parse.");
            System.out.println("\t-st   : Print the symbol table after successful parse.");
            System.out.println("\t-ir   : Print the IR code. May not be used with -irc flag.");
            System.out.println("\t-irc  : Print the IR code with comments to make it more human readable.\n\t        May not be used with -ir flag.");
            System.out.println("\t-all  : Shorthand for -t -p -st -ir. May not be used with -irc or -allc flags.");
            System.out.println("\t-allc : Shorthand for -t -p -st -irc. May not be used with -ir or -all flags.");
            System.out.println("\t-o    : Output to file named \"<input-file>.out\". May not be used unless\n\t        other flag(s) are set.");
            System.out.println("\t-r1   : Use naive register allocation. May not be used with -r2 flag.");
            System.out.println("\t-r2   : Use basic block graph coloring register allocation (default). May not be used with -r1 flag.");

            System.exit(0);
        }

        TigerScanner scanner = new TigerScanner(args[0]);

        //if we made it this far, it means scanner didn't throw a file not found error,
        //so we are safe to set the file output for Output
        if(Output.printToFile)
        {
            String outputFile = args[0].substring(0, args[0].length() - ".tiger".length());
            outputFile += ".out";
            Output.setOutputFileName(outputFile);
        }

        TigerParser parser = new TigerParser(scanner);

        parser.parse();

        ParseTreeNode parseTreeRoot = parser.getParseTree();
        Map<String, Symbol> globalSymbolTable = parser.getGlobalSymbolTable();




        if(parseTreeRoot != null)
        {
            //PRINT SYMBOL TABLE
            Output.symbolTablePrintln("");
            for (String symbolId : globalSymbolTable.keySet())
            {
                Output.symbolTablePrintln(globalSymbolTable.get(symbolId) + "\n");
            }


            //PRINT PARSE TREE
            Output.parseTreePrintln("");
            Output.parseTreePrintln(parseTreeRoot.nodeToString(0));

            TigerSemanticAnalyzer semanticAnalyzer = new TigerSemanticAnalyzer(parseTreeRoot, globalSymbolTable);
            String[] errors = semanticAnalyzer.analyze();

            if (errors.length == 0)
            {
                TigerIrGenerator irGenerator = new TigerIrGenerator(parseTreeRoot, semanticAnalyzer.getParseTreeAttributes(), globalSymbolTable);
                String[] ir = irGenerator.generateCode();

                //PRINT IR CODE
                Output.irPrintln("");
                for (String codeLine : ir)
                {
                    //Only print comments in debug mode
                    if (codeLine.trim().startsWith("#"))
                    {
                        if (codeLine.length() == 1)
                            Output.irPrintln("");
                        else
                            Output.irPrintln(codeLine);
                    }
                    else
                    {
                        Output.irPrintln(codeLine);
                    }
                }

                String[] irWithAllocation = null;

                if (regAllocScheme == 1)
                {
                    Output.irPrintln("\n\n==================================");
                    Output.irPrintln("---------Naive Allocation---------");
                    Output.irPrintln("==================================\n\n");

                    //ADD REGISTER LOAD/STORES INTO IR-CODE
                    NaiveRegisterAllocator naiveAllocator = new NaiveRegisterAllocator(ir);
                    irWithAllocation = naiveAllocator.insertAllocationStatements();

                    //PRINT IR CODE WITH ALLOCATION STATEMENTS
                    Output.irPrintln("");
                    for (String codeLine : irWithAllocation)
                    {
                        //Only print comments in debug mode
                        if (codeLine.trim().startsWith("#"))
                        {
                            if (codeLine.length() == 1)
                                Output.irPrintln("");
                            else
                                Output.irPrintln(codeLine);
                        }
                        else
                        {
                            Output.irPrintln(codeLine);
                        }
                    }
                }
                else if (regAllocScheme == 2)
                {

                    Output.irPrintln("\n\n==================================");
                    Output.irPrintln("------Basic Block Allocation------");
                    Output.irPrintln("==================================\n\n");

                    BasicBlockAllocator basicBlockAllocator = new BasicBlockAllocator(ir);
                    irWithAllocation = basicBlockAllocator.insertAllocationStatements();

                    //PRINT IR CODE WITH ALLOCATION STATEMENTS
                    Output.irPrintln("");
                    for (String codeLine : irWithAllocation)
                    {
                        //Only print comments in debug mode
                        if (codeLine.trim().startsWith("#"))
                        {
                            if (codeLine.length() == 1)
                                Output.irPrintln("");
                            else
                                Output.irPrintln(codeLine);
                        }
                        else
                        {
                            Output.irPrintln(codeLine);
                        }
                    }
                }

                AssemblyGenerator asmGenerator = new AssemblyGenerator(irWithAllocation);
                String[] assemblyCode = asmGenerator.produceAssembly();

                String fileName = args[0].substring(0, args[0].length() - ".tiger".length());
                fileName += ".s";

                PrintWriter asmPw = null;

                try
                {
                    asmPw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
                }
                catch (Exception e)
                {
                    Output.println("Error opening file writer. Unable to produce assembly file.");
                    System.exit(-1);
                }

                for(String codeLine: assemblyCode)
                {
                    asmPw.println(codeLine);
                }

                asmPw.close();

                Output.println("\n\nAssembly output to " + fileName);
            }
            else
            {
                for (String error : errors)
                {
                    Output.println(error + "\n");
                }

                Output.irPrintln("Semantic analysis failed. Cannot print IR code.\n");
            }
        }
        else
        {
            Output.symbolTablePrintln("\nParse failed. Cannot print symbol table.");
            Output.parseTreePrintln("Parse failed. Cannot print parse tree.");
            Output.irPrintln("Parse failed. Cannot print IR code.");
        }

        //store this before closing the file
        String outFileName = Output.getOutputFileName();

        Output.closeOutputFile();
        Output.println("\nComplete.");


        if(Output.printToFile)
        {
            if(outFileName == null)
            {
                Output.println("Unable to print output to file.");
            }
            else
            {
                Output.println("Output printed to file " + outFileName);
            }
        }
    }
}
