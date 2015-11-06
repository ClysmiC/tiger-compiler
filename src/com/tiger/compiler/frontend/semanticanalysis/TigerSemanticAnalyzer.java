package com.tiger.compiler.frontend.semanticanalysis;

import com.tiger.compiler.Output;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;
import com.tiger.compiler.frontend.parser.NonterminalSymbol;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.TypeSymbol;
import com.tiger.compiler.frontend.parser.symboltable.VariableSymbol;

import java.lang.reflect.Type;
import java.util.*;

public class TigerSemanticAnalyzer
{
    private ParseTreeNode parseTreeRoot;
    private Map<String, Symbol> globalSymbolTable;
    private Map<String, Map<String, Symbol>> functionSymbolTables;

    private Map<ParseTreeNode, Map<String, Object>> attributes;
    private List<String> semanticErrors;

    public TigerSemanticAnalyzer(ParseTreeNode parseTreeRoot, Map<String, Symbol> globalSymbolTable,
                                 Map<String, Map<String, Symbol>> functionSymbolTables)
    {
        this.parseTreeRoot = parseTreeRoot;
        this.globalSymbolTable = globalSymbolTable;
        this.functionSymbolTables = functionSymbolTables;

        attributes = new HashMap<>();
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

        switch(nodeTypeStr) {
            /***********************
             * TOKENS
             **********************/

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
            case "GREATEREQ": {
                return;
            }

            case "ID": {
                //ID's can decorate their own node, but they don't perform any real semantic actions

                //inherit function symbol table from parent
                ParseTreeNode parent = node.getParent();
                Map<String, Object> parentAttributes = attributes.get(parent);
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");

                VariableSymbol variable;
                String id = node.getLiteralToken();

                //check function symbol table first
                if (functionSymbolTable != null && functionSymbolTable.containsKey(id)) {
                    variable = (VariableSymbol) functionSymbolTable.get(id);
                } else if (globalSymbolTable.containsKey(id)) {
                    variable = (VariableSymbol) globalSymbolTable.get(id);
                } else {
                    semanticErrors.add("Undeclared identifier \"" + id + "\"");
                    return;
                }

                TypeSymbol myType = variable.getType();

                Map<String, Object> myAttributes = new HashMap<>();
                myAttributes.put("type", myType);
                attributes.put(node, myAttributes);
            }
            break;

            case "INTLIT": {
                Map<String, Object> myAttributes = new HashMap<>();
                myAttributes.put("type", TypeSymbol.INT);
                attributes.put(node, myAttributes);
            }
            break;

            case "FLOATLIT": {
                Map<String, Object> myAttributes = new HashMap<>();
                myAttributes.put("type", TypeSymbol.FLOAT);
                attributes.put(node, myAttributes);
            }
            break;


            /**************************
             * NONTERMINALS
             **************************/
            case "TIGER_PROGRAM":
            case "DECLARATION_SEGMENT":
            case "TYPE_DECLARATION_LIST":
            case "VAR_DECLARATION_LIST":
            case "FUNC_DECLARATION_LIST":
            case "TYPE_DECLARATION":
            case "TYPE_SYMBOL":
            case "TYPE_ID":
            case "VAR_DECLARATION":
            case "ID_LIST":
            case "ID_LIST_TAIL":
            case "OPTIONAL_INIT":
            case "FUNC_DECLARATION":
            case "PARAM_LIST":
            case "PARAM_LIST_TAIL":
            case "RET_TYPE":
            case "PARAM":
            case "STAT_SEQ":
            case "STAT_SEQ_CONT":
            {
                //Initialize your attributes and add them to the map
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }

            } break;

            case "STAT":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>)parentAttributes.get("functionSymbolTable");

                myAttributes.put("functionSymbolTable", functionSymbolTable);


                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }


                //Semantic check results of children analyses, and add remaining attributes to self

                //<STAT> -> ID <STAT_ASSIGN_OR_FUNC> SEMI
                if(children.get(0).getNodeType() == Token.ID)
                {
                    String childId = children.get(0).getLiteralToken();

                    Symbol childSymbol;
                    TypeSymbol lhsType;

                    if(functionSymbolTable != null && functionSymbolTable.containsKey(childId))
                    {
                        childSymbol = functionSymbolTable.get(childId);
                    }
                    else if(globalSymbolTable.containsKey(childId))
                    {
                        childSymbol = globalSymbolTable.get(childId);
                    }
                    else
                    {
                        //error was added when id was being analyzed
                        //doesn't make sense to try to type match a rhs to a non-existant variable, just return
                        return;
                    }

                    ParseTreeNode statAssignOrFuncNode = children.get(1);
                    Map<String, Object> statAssignOrFuncAttributes = attributes.get(statAssignOrFuncNode);

                    if((boolean)statAssignOrFuncAttributes.get("isAssignment"))
                    {
                        //type match lhs and rhs

                        if(childSymbol instanceof VariableSymbol)
                        {
                            lhsType = ((VariableSymbol)childSymbol).getType();
                        }
                        else
                        {
                            semanticErrors.add("Cannot assign values to function or type identifier.");
                            return;
                        }

                        TypeSymbol rhsType = (TypeSymbol)statAssignOrFuncAttributes.get("type");

                        if(isTypeCompatibleAssignment(lhsType, rhsType))
                        {
                            //compatible!
                            return;
                        }
                        else
                        {
                            semanticErrors.add("Could not assign type " + rhsType.getName() + " to variable " + childId
                                    + " of type " + lhsType.getName());

                            return;
                        }
                    }
                    else
                    {
                        //STAT_ASSIGN_OR_FUNC is a function

                        //this node doesn't know the function id, so it can't determine the return type.
                        //the _parent_ will read this node's "isAssignment" attribute, and if that is false,
                        //the parent will know that the id is a function and will be able to assign itself
                        //a type based on the function's return type

                        return;
                    }
                }
            } break;

            case "STAT_ASSIGN_OR_FUNC":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>)parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }


                //Semantic check results of children analyses, and add remaining attributes to self

                //<STAT_ASSIGN_OR_FUNC> -> <LVALUE_TAIL> ASSIGN <STAT_ASSIGN_RHS>
                if(children.get(0).getNodeType() == NonterminalSymbol.LVALUE_TAIL)
                {
                    myAttributes.put("isAssignment", true);

                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(0));
                    myAttributes.put("index", lValueTailAttributes.get("index"));

                    Map<String, Object> statAssignRhsAttributes = attributes.get(children.get(2));
                    myAttributes.put("type", statAssignRhsAttributes.get("type"));
                }

            } break;

            case "STAT_ASSIGN_RHS":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>)parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }


                //Semantic check results of children analyses, and add remaining attributes to self

                //<STAT_ASSIGN_RHS> -> <CONST> <PRIME_TERM>
                if(children.get(0).getNodeType() == NonterminalSymbol.CONST)
                {
                    Map<String, Object> constAttributes = attributes.get(children.get(0));
                    TypeSymbol constType = (TypeSymbol)constAttributes.get("type");

                    Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                    TypeSymbol primeTermType = (TypeSymbol)primeTermAttributes.get("type");

                    TypeSymbol resultType = inferType(constType, primeTermType);

                    if(resultType != null)
                    {
                        myAttributes.put("type", resultType);
                    }
                    else
                    {
                        semanticErrors.add("Operation between incompatible types: \"" + constType + "\" and \"" + primeTermType + "\"");
                        return;
                    }
                }
            } break;

            case "EXPR_OR_FUNC_END":
            case "FUNC_CALL_END":
            case "IF_STAT":
            case "IF_END":
            case "EXPR":

            case "CONST":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                analyze(children.get(0));

                Map<String, Object> childAttributes = attributes.get(children.get(0));

                myAttributes.put("type", childAttributes.get("type"));

            } break;

            case "TERM1":
            case "TERM2":
            case "TERM3":
            case "TERM4":
            case "TERM5":
            case "FACTOR":
            case "INEQUALITY_OP":
            case "EQUALITY_OP":
            case "ADD_SUB_OP":
            case "MUL_DIV_OP":
            case "EXPR_LIST":
            case "EXPR_LIST_TAIL":
            case "LVALUE_TAIL":
            case "EXPR_PRIME":
            case "TERM1_PRIME":
            case "TERM2_PRIME":
            case "TERM3_PRIME":
            case "TERM4_PRIME":
            case "TERM5_PRIME":
            case "PRIME_TERM":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>)parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }

                //<PRIME_TERM> -> NULL
                if(children.isEmpty())
                {
                    myAttributes.put("type", null);
                }
                //ALL OTHER CASES
                else
                {
                    Map<String, Object> firstTermAttributes = attributes.get(children.get(1));
                    Map<String, Object> secondTermAttributes = attributes.get(children.get(2));

                    TypeSymbol type1 = (TypeSymbol)firstTermAttributes.get("type");
                    TypeSymbol type2 = (TypeSymbol)secondTermAttributes.get("type");

                    TypeSymbol result = inferType(type1, type2);

                    if(result != null)
                    {
                        myAttributes.put("type", children.get(1));
                    }
                    else
                    {
                        semanticErrors.add("Operation between incompatible types: \"" + type1 + "\" and \"" + type2 + "\"");
                        return;
                    }
                }
            } break;
        }
    }

    private boolean isTypeCompatibleAssignment(TypeSymbol lhsType, TypeSymbol rhsType)
    {
        return (lhsType == TypeSymbol.FLOAT && rhsType == TypeSymbol.INT) || lhsType == rhsType;
    }

    /**
     * Determine the resulting type of performing an operation between the two given types.
     * Type2 may be null, in which case Type1 is returned. Type1 should not be null. If the types
     * are incompatible, return null.
     *
     * @param type1
     * @param type2
     * @return
     */
    private TypeSymbol inferType(TypeSymbol type1, TypeSymbol type2)
    {
        if(type2 == null)
            return type1;

        if(type1 == type2)
            return type1;

        if((type1 == TypeSymbol.INT && type2 == TypeSymbol.FLOAT) || (type1 == TypeSymbol.FLOAT && type2 == TypeSymbol.INT))
            return TypeSymbol.FLOAT;

        return null;
    }
}
