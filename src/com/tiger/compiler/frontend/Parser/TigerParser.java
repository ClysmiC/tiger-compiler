package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Output;
import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.TypeSymbol;
import com.tiger.compiler.frontend.parser.symboltable.VariableSymbol;
import com.tiger.compiler.frontend.scanner.TigerScanner;
import com.tiger.compiler.frontend.Token;

import java.lang.reflect.Type;
import java.util.*;

public class TigerParser
{
    private TigerScanner tigerScanner;
    private ParserTable parserTable;

    //Hold onto certain values so that if a semantic action is required (such as putting
    //something in a symbol table), we have all the info we need.
    private String latestId;
    private TypeSymbol latestType;

    private int latestIntLit;
    private float latestFloatLit;

    private Stack<Object> semanticStack;

    /**
     * Because of the way Tiger is structured, everything is global, EXCEPT
     * parameters to functions, which are scoped to that function. These params
     * exist in the functionSymbolTable, everything else is in the globalSymbolTable
     */
    private Map<String, Symbol> globalSymbolTable;
    private Map<String, Map<String, Symbol>> functionSymbolTables;

    public TigerParser(TigerScanner scanner)
    {
        tigerScanner = scanner;
        parserTable = ParserTableGenerator.generateParserTable();

        globalSymbolTable = new HashMap<>();
        globalSymbolTable.put("int", TypeSymbol.INT);
        globalSymbolTable.put("float", TypeSymbol.FLOAT);

        functionSymbolTables = new HashMap<>();

        semanticStack = new Stack<>();
    }

    public void parse()
    {
        Enum focus;
        Enum lookAhead;

        Stack<Enum> stack = new Stack<>();
        stack.add(Token.EOF);
        stack.add(NonterminalSymbol.TIGER_PROGRAM);

        focus = stack.peek();

        Tuple<Token, String> token;

        token = tigerScanner.nextToken();
        Output.debugPrintln(token.x.toString());

        lookAhead = token.x;

        while (true)
        {
            if (focus == Token.EOF && lookAhead == Token.EOF)
            {
                Output.println(!tigerScanner.isErrorRaised() ? "\nSuccessful parse\n" : "\nUnsuccessful parse");

                //print out the entire symbol table (for testing)
                for(String symbolId: globalSymbolTable.keySet())
                {
                    System.out.println(globalSymbolTable.get(symbolId) + "\n");
                }

                System.exit(0);
            }
            else if (focus instanceof Token)
            {
                if (focus == lookAhead)
                {
                    if(focus == Token.ID)
                    {
                        latestId = token.y;

                        if(globalSymbolTable.containsKey(latestId))
                        {
                            Symbol symbol = globalSymbolTable.get(latestId);

                            if(symbol instanceof TypeSymbol)
                            {
                                latestType = (TypeSymbol)symbol;
                            }
                        }
                    }
                    else if (focus == Token.INT)
                    {
                        latestType = TypeSymbol.INT;
                    }
                    else if (focus == Token.FLOAT)
                    {
                        latestType = TypeSymbol.FLOAT;
                    }
                    else if (focus == Token.INTLIT)
                    {
                        latestIntLit = new Integer(token.y);
                    }
                    else if (focus == Token.FLOATLIT)
                    {
                        latestFloatLit = new Float(token.y);
                    }

                    stack.pop();
                    token = tigerScanner.nextToken();
                    lookAhead = token.x;

                    Output.debugPrintln(token.x.toString());
                }
                else
                {
                    String errorString = "(parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;
                    errorString += "\nExpected token: " + focus;

                    stopParsingAndExit(errorString);
                }
            }
            else if (focus instanceof NonterminalSymbol)
            {
                ParserProduction prod = parserTable.lookup(focus, lookAhead);

                if (prod == ParserProduction.ERROR)
                {
                    String errorString = "(parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;

                    //TODO: suggest tokens
                    //TODO: recover from error and continue parse

                    stopParsingAndExit(errorString);
                }

                stack.pop();

                List<Enum> rhs = prod.getRhs();

                for (int i = rhs.size() - 1; i >= 0; i--)
                {
                    if(rhs.get(i) != Token.NULL)
                        stack.push(rhs.get(i));
                }
            }
            else if(focus instanceof SemanticAction)
            {
                SemanticAction action = (SemanticAction)focus;

                switch(action)
                {
                    case PUT_ID_STACK:
                    {
                        semanticStack.push(latestId);
                    } break;

                    case PUT_TYPE_STACK:
                    {
                        semanticStack.push(latestType);
                    } break;

                    case PUT_INT_STACK:
                    {
                        semanticStack.push(latestIntLit);
                    } break;

                    case PUT_TRUE_STACK:
                    {
                        semanticStack.push(true);
                    } break;

                    case PUT_FALSE_STACK:
                    {
                        semanticStack.push(false);
                    } break;

                    case PUT_TYPE_TABLE:
                    {
                        TypeSymbol baseType = (TypeSymbol)semanticStack.pop();
                        boolean isArray = (boolean)semanticStack.pop();
                        int arraySize = 0;

                        if(isArray)
                        {
                            arraySize = (int)semanticStack.pop();
                        }

                        String id = (String)semanticStack.pop();

                        assert semanticStack.isEmpty();

                        if(globalSymbolTable.containsKey(id))
                        {
                            System.out.println("ERROR"); //TODO: better error behavior
                        }
                        else
                        {
                            if(isArray)
                                globalSymbolTable.put(id, new TypeSymbol(id, baseType, arraySize));
                            else
                                globalSymbolTable.put(id, new TypeSymbol(id, baseType));
                        }

                    } break;

                    case PUT_VARS_TABLE:
                    {

                    } break;

                    case PUT_FUNC_TABLE:
                    {

                    } break;
                }

                stack.pop();
            }

            focus = stack.peek();
        }
    }

    private void stopParsingAndExit(String debugMessage)
    {
        if(!debugMessage.isEmpty())
            Output.println("\n" + debugMessage + "\n");

        //print the rest of the scan. useful for detecting multiple scanner errors
        while(true)
        {

            Tuple<Token, String> token = tigerScanner.nextToken();

            //print token type
            if(token.x != Token.ERROR)
            {
                Output.debugPrintln(token.x.toString());
            }
            else
            {
                Output.println("\n" + token.y + "\n"); //error message
            }

            if(token.x == Token.EOF)
                break;
        }

        Output.println("\nUnsuccessful parse");

        System.exit(1);
    }
}
