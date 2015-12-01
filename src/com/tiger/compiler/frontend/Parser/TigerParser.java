package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Output;
import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.FunctionSymbol;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.TypeSymbol;
import com.tiger.compiler.frontend.parser.symboltable.VariableSymbol;
import com.tiger.compiler.frontend.scanner.TigerScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class TigerParser
{
    private TigerScanner tigerScanner;
    private ParserTable parserTable;

    private GrammarSymbol focus;
    private GrammarSymbol lookAhead;

    private Stack<GrammarSymbol> stack;

    //Hold onto certain values so that if a semantic action is required (such as putting
    //something in a symbol table), we have all the info we need.
    private String latestId;
    private TypeSymbol latestType;

    private int latestIntLit;
    private float latestFloatLit;

    private Stack<Object> semanticStack;
    int loopLevel; //ensures "break" gets called at valid spot
    private boolean error;

    //error so bad that we can't continue to parse (eg, unable to match a rule while there aren't any
    //semicolons on the parse stack to fall back and recover to
    private boolean fatalError;

    /**
     * Because of the way Tiger is structured, everything is global, EXCEPT
     * parameters to functions, which are scoped to that function. These params
     * exist in the functionSymbolTable, everything else is in the globalSymbolTable
     */
    private Map<String, Symbol> globalSymbolTable;

    private ParseTreeNode parseTreeRoot;
    private ParseTreeNode parseTreeFocus;

    public TigerParser(TigerScanner scanner)
    {
        tigerScanner = scanner;
        parserTable = ParserTableGenerator.generateParserTable();

        globalSymbolTable = new HashMap<>();
        globalSymbolTable.put("int", TypeSymbol.INT);
        globalSymbolTable.put("float", TypeSymbol.FLOAT);

        List<TypeSymbol> printiArgs = new ArrayList<>();
        printiArgs.add(TypeSymbol.INT);
        FunctionSymbol printi = new FunctionSymbol("printi", null, printiArgs);

        List<TypeSymbol> printfArgs = new ArrayList<>();
        printfArgs.add(TypeSymbol.FLOAT);
        FunctionSymbol printf = new FunctionSymbol("printf", null, printfArgs);

        FunctionSymbol flush = new FunctionSymbol("flush", null, new ArrayList<TypeSymbol>());

        List<TypeSymbol> notArgs = new ArrayList<>();
        notArgs.add(TypeSymbol.INT);
        FunctionSymbol not = new FunctionSymbol("not", TypeSymbol.INT, notArgs);

        List<TypeSymbol> exitArgs = new ArrayList<>();
        exitArgs.add(TypeSymbol.INT);
        FunctionSymbol exit = new FunctionSymbol("exit", null, exitArgs);

        globalSymbolTable.put("printi", printi);
        globalSymbolTable.put("printf", printf);
        globalSymbolTable.put("flush", flush);
        globalSymbolTable.put("not", not);
        globalSymbolTable.put("exit", exit);

        semanticStack = new Stack<>();
        loopLevel = 0;

        error = false;
        fatalError = false;
    }

    public void parse()
    {
        stack = new Stack<>();
        stack.add(Token.EOF);
        stack.add(NonterminalSymbol.TIGER_PROGRAM);

        parseTreeRoot = new ParseTreeNode(null, NonterminalSymbol.TIGER_PROGRAM, null, tigerScanner.getLineNum());
        parseTreeFocus = parseTreeRoot;

        focus = stack.peek();

        Tuple<Token, String> token;

        token = tigerScanner.nextToken();
        Output.tokenPrintln(token.x.name());

        lookAhead = token.x;

        while (true)
        {
            if(fatalError)
                return;

            if (focus == Token.EOF && lookAhead == Token.EOF)
            {
                if(tigerScanner.isErrorRaised())
                    error = true;
                return; //done parsing :)
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

                    //add the actual id, intlit, floatlit, etc. to the parse tree nodes
                    parseTreeFocus.setLiteralToken(token.y);
                    parseTreeFocus = parseTreeFocus.nextNodePreOrder();

                    token = tigerScanner.nextToken();
                    lookAhead = token.x;

                    Output.tokenPrintln(token.x.name());
                }
                else
                {
                    String errorString = "(parser error) Line: " + tigerScanner.getLineNum() + "\n" + tigerScanner.getPartialPrefix() + "<---";
                    errorString += "\nUnexpected token found: " + lookAhead;
                    errorString += "\nExpected token: " + focus;

                    reportErrorAndSkipAhead(errorString);
                    continue;
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

                    reportErrorAndSkipAhead(errorString);
                    continue;
                }

                stack.pop();

                List<GrammarSymbol> rhs = prod.getRhs();

                List<ParseTreeNode> parseTreeChildren = new ArrayList<>();

                //add symbols to the parse tree
                for(GrammarSymbol symbol: rhs)
                {
                    if(symbol instanceof SemanticAction || symbol == Token.NULL)
                        continue;

                    //we will add the literal token to the node once we match it, assume it is null for now
                    parseTreeChildren.add(new ParseTreeNode(parseTreeFocus, symbol, null, tigerScanner.getLineNum()));
                }

                parseTreeFocus.setChildren(parseTreeChildren);

                //travel to the next node (should mirror behavior of parse stack)
                parseTreeFocus = parseTreeFocus.nextNodePreOrder();

                //add symbols to the stack in reverse order
                for (int i = rhs.size() - 1; i >= 0; i--)
                {
                    if(rhs.get(i) != Token.NULL)
                    {
                        stack.push(rhs.get(i));
                    }
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
                        TypeSymbol derivedType = (TypeSymbol)semanticStack.pop();
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
                            Output.println("(symbol table error): Line " + tigerScanner.getLineNum());
                            Output.println("\"" + id + "\" already exists in symbol table. Could not create new type\n");
                            error = true;
                        }
                        else
                        {
                            if(isArray)
                                globalSymbolTable.put(id, new TypeSymbol(id, derivedType, arraySize));
                            else
                                globalSymbolTable.put(id, new TypeSymbol(id, derivedType));
                        }

                    } break;

                    case PUT_VARS_TABLE:
                    {
                        TypeSymbol type = (TypeSymbol)semanticStack.pop();

                        boolean throwaway = (boolean)semanticStack.pop();
                        assert throwaway == false; //always false to indicate not an array
                        assert !semanticStack.isEmpty(); //should always be at least 1 id

                        while(!semanticStack.isEmpty())
                        {
                            String id = (String)semanticStack.pop();

                            if(globalSymbolTable.containsKey(id))
                            {
                                Output.println("(symbol table error): Line " + tigerScanner.getLineNum());
                                Output.println("\"" + id + "\" already exists in symbol table. Could not create new variable\n");
                                error = true;
                            }
                            else
                            {
                                globalSymbolTable.put(id, new VariableSymbol(id, type));
                            }
                        }
                    } break;

                    case PUT_FUNC_TABLE:
                    {
                        boolean hasReturnVal = (boolean)semanticStack.pop();
                        TypeSymbol returnType = null;

                        if(hasReturnVal)
                        {
                            returnType = (TypeSymbol)semanticStack.pop();
                            boolean throwaway = (boolean)semanticStack.pop();
                            assert throwaway == false;
                        }

                        assert !semanticStack.isEmpty();

                        //temporarily keep track of all the parameters as we
                        //pop them off the stack so we can add their types
                        //to the function symbol and add them to the
                        //function's symbol table
                        ArrayList<VariableSymbol> parameters = new ArrayList<>();

                        while(!semanticStack.isEmpty())
                        {
                            Object paramTypeOrFuncName = semanticStack.pop();

                            if(paramTypeOrFuncName instanceof TypeSymbol)
                            {
                                TypeSymbol paramType = (TypeSymbol)paramTypeOrFuncName;
                                boolean throwaway = (boolean)semanticStack.pop();
                                assert throwaway == false;
                                String paramName = (String)semanticStack.pop();

                                parameters.add(new VariableSymbol(paramName, paramType));
                            }
                            else if (paramTypeOrFuncName instanceof String)
                            {
                                String id = (String)paramTypeOrFuncName;
                                List<TypeSymbol> types = new ArrayList<>(parameters.size());

                                //add parameters to the function's Type list in reverse
                                //(since the last parameter was on the top of the stack
                                for(int i = parameters.size() - 1; i >= 0; i--)
                                {
                                    types.add(parameters.get(i).getType());
                                }

                                FunctionSymbol function = new FunctionSymbol(id, returnType, types);
                                Map<String, Symbol> functionSymbolTable = function.getSymbolTable();

                                //build function symbol table conitaining all the params
                                for(VariableSymbol param: parameters)
                                {
                                    if(functionSymbolTable.containsKey(param.getName()))
                                    {
                                        Output.println("(symbol table error): Line " + tigerScanner.getLineNum());
                                        Output.println("Repeat parameter name \"" + param.getName() + "\" in function \"" + function.getName() + "\"\n");
                                        error = true;
                                    }

                                    functionSymbolTable.put(param.getName(), param);
                                }

                                if(globalSymbolTable.containsKey(id))
                                {
                                    Output.println("(symbol table error): Line " + tigerScanner.getLineNum());
                                    Output.println("\"" + id + "\" already exists in symbol table. Could not create new variable.\n");
                                    error = true;
                                }
                                else
                                {
                                    globalSymbolTable.put(id, function);
                                }
                            }
                            else
                            {
                                //ERROR
                            }
                        }
                    } break;

                    case LOOP_ENTER:
                    {
                        loopLevel++;
                    } break;

                    case LOOP_EXIT:
                    {
                        loopLevel--;
                    } break;

                    case LOOP_BREAK:
                    {
                        if (loopLevel == 0)
                        {
                            Output.println("(error): Line " + tigerScanner.getLineNum());
                            Output.println("\"break\" may only be used within a loop.\n");
                            error = true;
                        }
                    }
                }

                stack.pop();
            }

            focus = stack.peek();
        }
    }

    /**
     * Prints the reported error, and then skips any tokens/rules until a semi colon is reached. Begins parsing again
     * from that point
     * @param errorString
     */
    private void reportErrorAndSkipAhead(String errorString)
    {
        error = true;
        Output.println("\n" + errorString + "\n");

        GrammarSymbol symbol;

        symbol = stack.pop();
        while(symbol != Token.SEMI && symbol != Token.EOF)
        {
            if(stack.isEmpty())
            {
                fatalError = true;
                return;
            }

            symbol = stack.pop();
        }

        if(symbol == Token.EOF)
        {
            //parse wasn't far enough to really have a point to recover to
            fatalError = true;
            return;
        }

        Token token = tigerScanner.nextToken().x;
        Output.tokenPrintln(token.name());

        while(token != Token.SEMI && token != Token.EOF)
        {
            token = tigerScanner.nextToken().x;
            Output.tokenPrintln(token.name());
        }

        if(token == Token.EOF)
        {
            Output.println("(parser error): Line " + tigerScanner.getLineNum());
            Output.println("Unexpected end of file reached.");
            fatalError = true;
            return;
        }

        lookAhead = tigerScanner.nextToken().x;
        focus = stack.peek();
    }

    /**
     * Must be called after parse()
     */
    public ParseTreeNode getParseTree()
    {
        if(error)
            return null;

        return parseTreeRoot;
    }

    /**
     *
     */
    public Map<String, Symbol> getGlobalSymbolTable()
    {
        return globalSymbolTable;
    }
}
