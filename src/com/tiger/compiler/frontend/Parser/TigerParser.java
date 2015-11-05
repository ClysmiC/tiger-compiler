package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Output;
import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.FunctionSymbol;
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

    private ParseTreeNode parseTreeRoot;
    private ParseTreeNode parseTreeFocus;

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
        GrammarSymbol focus;
        GrammarSymbol lookAhead;

        Stack<GrammarSymbol> stack = new Stack<>();
        stack.add(Token.EOF);
        stack.add(NonterminalSymbol.TIGER_PROGRAM);

        parseTreeRoot = new ParseTreeNode(null, NonterminalSymbol.TIGER_PROGRAM);
        parseTreeFocus = parseTreeRoot;

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
                    token = tigerScanner.nextToken();
                    lookAhead = token.x;

                    parseTreeFocus = parseTreeFocus.nextNodePreOrder();

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

                List<GrammarSymbol> rhs = prod.getRhs();

                List<ParseTreeNode> parseTreeChildren = new ArrayList<>();

                //add symbols to the parse tree
                for(GrammarSymbol symbol: rhs)
                {
                    if(symbol instanceof SemanticAction || symbol == Token.NULL)
                        continue;

                    parseTreeChildren.add(new ParseTreeNode(parseTreeFocus, symbol));
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
                            System.out.println("Error: \"" + id + "\" already exists in symbol table." +
                                    "Could not create new type");
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
                        TypeSymbol type = (TypeSymbol)semanticStack.pop();

                        boolean throwaway = (boolean)semanticStack.pop();
                        assert throwaway == false; //always false to indicate not an array
                        assert !semanticStack.isEmpty(); //should always be at least 1 id

                        while(!semanticStack.isEmpty())
                        {
                            String id = (String)semanticStack.pop();

                            if(globalSymbolTable.containsKey(id))
                            {
                                System.out.println("Error: \"" + id + "\" already exists in symbol table." +
                                        "Could not create new variable");
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
                                        Output.println("Error: Repeat parameter name \"" + param.getName() +
                                                "\" in function \"" + function.getName() + "\"");
                                        break;
                                    }

                                    functionSymbolTable.put(param.getName(), param);
                                }

                                if(globalSymbolTable.containsKey(id))
                                {
                                    Output.println("Error: \"" + id + "\" already exists in symbol table." +
                                            "Could not create new function.");
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

    /**
     * Must be called after parse()
     */
    public ParseTreeNode getParseTree()
    {
        return parseTreeRoot;
    }

    /**
     *
     */
    public Map<String, Symbol> getGlobalSymbolTable()
    {
        return globalSymbolTable;
    }

    public Map<String, java.util.Map<String, Symbol>> getFunctionSymbolTables()
    {
        return functionSymbolTables;
    }
}
