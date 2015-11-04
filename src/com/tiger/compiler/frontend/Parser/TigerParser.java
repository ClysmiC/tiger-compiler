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
    private boolean latestTypeIsArray;

    private boolean initializingVar;
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
                Output.println(!tigerScanner.isErrorRaised() ? "\nSuccessful parse" : "\nUnsuccessful parse");
                System.exit(0);
            }
            else if (focus instanceof Token)
            {
                if (focus == lookAhead)
                {
                    if(focus == Token.ID)
                    {
                        latestId = token.y;
                    }
                    else if (focus == Token.OF)
                    {
                        latestTypeIsArray = true;
                    }
                    else if (focus == Token.SEMI)
                    {
                        latestTypeIsArray = false;
                        initializingVar = false;
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
                        System.out.println("Int lit: " + token.y);
                    }
                    else if (focus == Token.FLOATLIT)
                    {
                        System.out.println("Float lit: " + token.y);
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
                else if (prod.getLhs() == NonterminalSymbol.OPTIONAL_INIT)
                {
                    final int DOING_OPTIONAL_INIT = 19;     //these numbers determined by ParserProductions.csv
                    final int SKIPPING_OPTIONAL_INIT = 20;

                    if(prod.getId() == DOING_OPTIONAL_INIT)
                    {
                        initializingVar = true;
                    }
                    else if(prod.getId() == SKIPPING_OPTIONAL_INIT)
                    {
                        initializingVar = false;
                    }
                    else
                    {
                        Output.println("Internal compiler error. Unknown production id \"" + prod.getId() + "\" when" +
                                "expanding OPTIONAL_INIT");
                    }
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

                    } break;

                    case CHECK_VALID_TYPE:
                    {
                        if(globalSymbolTable.containsKey(latestId) && globalSymbolTable.get(latestId) instanceof TypeSymbol)
                        {
                            latestType = (TypeSymbol)globalSymbolTable.get(latestId);
                            break;
                        }

                        Output.debugPrintln("ERROR [CHECK_VALID_TYPE]: Type \"" + latestId + "\" not found in symbol table.");

                    } break;

                    case APPEND_VAR_TO_LIST:
                    {
                        if(!globalSymbolTable.containsKey(latestId) && !idList.contains(latestId))
                        {
                            idList.add(latestId);
                            break;
                        }

                        Output.debugPrintln("ERROR [APPEND_VAR_TO_LIST]: Type \"" + latestId + "\" already exists in symbol table.");
                    } break;

                    case PUT_VAR_LIST:
                    {
                        for(String id: idList)
                        {
                            VariableSymbol symbol = new VariableSymbol(id, latestType, initializingVar);
                        }
                    }
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
