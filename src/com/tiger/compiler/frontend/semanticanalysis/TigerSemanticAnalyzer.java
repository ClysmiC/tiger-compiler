package com.tiger.compiler.frontend.semanticanalysis;

import com.tiger.compiler.Output;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;
import com.tiger.compiler.frontend.parser.NonterminalSymbol;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeAttributes;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.TypeSymbol;
import com.tiger.compiler.frontend.parser.symboltable.VariableSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TigerSemanticAnalyzer
{
    private ParseTreeNode parseTreeRoot;
    private Map<String, Symbol> globalSymbolTable;
    private Map<String, Map<String, Symbol>> functionSymbolTables;

    private Map<ParseTreeNode, ParseTreeAttributes> parseTreeDecorations;
    private List<String> semanticErrors;

    public TigerSemanticAnalyzer(ParseTreeNode parseTreeRoot, Map<String, Symbol> globalSymbolTable,
                                 Map<String, Map<String, Symbol>> functionSymbolTables)
    {
        this.parseTreeRoot = parseTreeRoot;
        this.globalSymbolTable = globalSymbolTable;
        this.functionSymbolTables = functionSymbolTables;

        parseTreeDecorations = new HashMap<ParseTreeNode, ParseTreeAttributes>();
        semanticErrors = new ArrayList<>();
    }

    /**
     * Walks the tree, performing semantic analysis.
     * Returns a list of errors discovered during the analysise. If the list is empty, the parse tree did not
     * have any semantic errors.
     *
     * @return
     */
    public String[] analyze()
    {
        analyze(parseTreeRoot);

        return semanticErrors.toArray(new String[semanticErrors.size()]);
    }

    private void analyze(ParseTreeNode node)
    {
        //since we are using an empty GrammarSymbol interface to store both Token enums and NonterminalSymbol enums
        //in the same collections, we can't really switch on the kind of GrammarSymbol. Instead, we can cast the
        //GrammarSymbol to the type it really is, and get the String name of it's value, then switch on that.
        String nodeTypeStr = "";
        GrammarSymbol nodeType = node.getNodeType();

        if(nodeType instanceof Token)
        {
            nodeTypeStr = ((Token)nodeType).toString();
        }
        else if(nodeType instanceof NonterminalSymbol)
        {
            nodeTypeStr = ((NonterminalSymbol)nodeType).toString();
        }
        else
        {
            Output.println("Fatal error: Compiler failed to cast parse tree node type to either a token or nonterminal." +
                    "Perhaps you put semantic actions in your parse tree?");
            System.exit(0);
        }

        //I'm 100% sure this could be made more elegant using polymorphism, but lets get it working first.

        switch(nodeTypeStr)
        {
            /**
             * TOKENS
             */

            //most tokens do nothing. ID, INTLIT, and FLOATLIT can decorate themselves with type
            case "COMMA":
            case "SEMI":
            case "LPAREN":
            case "RPAREN":
            case "LBRACE":
            case "RBRACE":
            case "LBRACK":
            case "RBRACK":
            case "COLON":
            case "PERIOD":
            case "PLUS":
            case "MINUS":
            case "MULT":
            case "DIV":
            case "EQ":
            case "LESSER":
            case "GREATER":
            case "AND":
            case "ARRAY":
            case "BREAK":
            case "DO":
            case "ELSE":
            case "END":
            case "FOR":
            case "FUNCTION":
            case "RETURN":
            case "IF":
            case "IN":
            case "LET":
            case "OF":
            case "THEN":
            case "TO":
            case "TYPE":
            case "VAR":
            case "INT":
            case "FLOAT":
            case "WHILE":
            case "ENDIF":
            case "BEGIN":
            case "ENDDO":
            case "OR":
            case "ASSIGN":
            case "NEQ":
            case "LESSEREQ":
            case "GREATEREQ":
            {
                return;
            }

            case "ID":
            {
                //ID's can decorate their own node, but they don't perform any real semantic actions

                //inherit function symbol table from parent
                ParseTreeNode parent = node.getParent();
                ParseTreeAttributes parentAttributes = parseTreeDecorations.get(parent);
                Map<String, Symbol> functionSymbolTable = parentAttributes.getFunctionSymbolTable();

                VariableSymbol variable;
                String id = node.getLiteralToken();

                //check function symbol table first
                if(functionSymbolTable != null && functionSymbolTable.containsKey(id))
                {
                    variable = (VariableSymbol)functionSymbolTable.get(id);
                }
                else if(globalSymbolTable.containsKey(id))
                {
                    variable = (VariableSymbol)globalSymbolTable.get(id);
                }
                else
                {
                    semanticErrors.add("Undeclared identifier \"" + id + "\"");
                    return;
                }

                TypeSymbol myType = variable.getType();

                parseTreeDecorations.put(node, new ParseTreeAttributes(globalSymbolTable, functionSymbolTable, myType));
            } break;

            case "INTLIT":
            {
                //we could grab function symbol table from parent, but this is a leaf node and no other node
                //will inherit or use it so we really don't care. All we want to do is decorate this
                //node with its type (the global symbol table decoration is also useless, but we already have a
                //reference to it, so why not?)
                parseTreeDecorations.put(node, new ParseTreeAttributes(globalSymbolTable, null, TypeSymbol.INT));
            } break;

            case "FLOATLIT":
            {
                //see comment for INTLIT
                parseTreeDecorations.put(node, new ParseTreeAttributes(globalSymbolTable, null, TypeSymbol.FLOAT));
            } break;


            /**
             * NONTERMINALS
             */
//            case
        }
    }
}
