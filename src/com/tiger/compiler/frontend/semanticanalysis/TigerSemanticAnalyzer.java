package com.tiger.compiler.frontend.semanticanalysis;

import com.tiger.compiler.Output;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;
import com.tiger.compiler.frontend.parser.NonterminalSymbol;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.FunctionSymbol;
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

    private Map<ParseTreeNode, Map<String, Object>> attributes;
    private List<String> semanticErrors;

    public TigerSemanticAnalyzer(ParseTreeNode parseTreeRoot, Map<String, Symbol> globalSymbolTable)
    {
        this.parseTreeRoot = parseTreeRoot;
        this.globalSymbolTable = globalSymbolTable;

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

    /**
     * Recursively walk down the tree and perform semantic analysis for each node.
     * The general strategy for analyzing a node is as follows:
     *   - Inherit necessary attributes from parent and assign known attributes to self
     *   - Recursively analyze children nodes
     *   - Ensure children nodes are compatible types (when necessary) and follow proper semantics. Assign additional
     *     attributes to self that are dependent on children node attributes.
     * @param node
     */
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
            System.exit(-1);
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
            case "PARAM_LIST":
            case "PARAM_LIST_TAIL":
            case "RET_TYPE":
            case "PARAM":
            case "STAT_SEQ":
            case "STAT_SEQ_CONT":
            case "INEQUALITY_OP":
            case "EQUALITY_OP":
            case "ADD_SUB_OP":
            case "MUL_DIV_OP":
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


            case "FUNC_DECLARATION":
            {
                //<FUNC_DECLARATION> -> FUNCTION ID LPAREN <PARAM_LIST> RPAREN <RET_TYPE> BEGIN <STAT_SEQ> END SEMI


                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();

                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }

                //Assign correct function symbol table to this node, so children node can inherit it
                String funcId = children.get(1).getLiteralToken();

                if(globalSymbolTable.containsKey(funcId))
                {
                    //failing this cast would be an internal compiler error, but i am confident it can't
                    //happen so I'm not logging it
                    FunctionSymbol func = (FunctionSymbol)globalSymbolTable.get(funcId);
                    myAttributes.put("functionSymbolTable", func.getSymbolTable());
                }
                else
                {
                    Output.println("Internal compiler error: Could not find function \"" + funcId + "\" in symbol table" +
                            " despite receiving it's name from a function declaration in the parse tree." );
                    System.exit(-1);
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

                        //no semantic action needed. this is simply a function being called for its side-effects...
                        //nothing is getting assigned or type-checked

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

                    Map<String, Object> statAssignRhsAttributes = attributes.get(children.get(2));
                    myAttributes.put("type", statAssignRhsAttributes.get("type"));
                }
                else
                {
                    myAttributes.put("isAssignment", false);
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
                //<STAT_ASSIGN_RHS> -> ID <EXPR_OR_FUNC_END>
                if(children.get(0).getNodeType() == Token.ID)
                {
                    Map<String, Object> exprOrFuncEndAttributes = attributes.get(children.get(1));

                    if((boolean)exprOrFuncEndAttributes.get("isFunction"))
                    {
                        //RHS is a function call.

                        String funcId = children.get(0).getLiteralToken();

                        if(!globalSymbolTable.containsKey(funcId))
                        {
                            semanticErrors.add("Undeclared identifier \"" + funcId + "\".");
                            myAttributes.put("type", null);
                            return;
                        }

                        //double check that the id they gave us was, in fact, a function
                        Symbol funcSymbol = globalSymbolTable.get(funcId);

                        if(!(funcSymbol instanceof FunctionSymbol))
                        {
                            semanticErrors.add("\"" + funcId + "\" is not a function.");
                            myAttributes.put("type", null);
                            return;
                        }

                        myAttributes.put("type", ((FunctionSymbol)funcSymbol).getReturnType());
                    }
                    else
                    {
                        //RHS is an expression

                        String varId = children.get(0).getLiteralToken();
                        TypeSymbol type1;

                        if(functionSymbolTable != null && functionSymbolTable.containsKey(varId))
                        {
                            //assume this cast is safe because function symbol table only contains parameters (variables)
                            type1 = ((VariableSymbol)functionSymbolTable.get(varId)).getType();
                        }
                        else if(globalSymbolTable.containsKey(varId))
                        {
                            Symbol symbol = globalSymbolTable.get(varId);

                            if(symbol instanceof VariableSymbol)
                            {
                                type1 = ((VariableSymbol)symbol).getType();
                            }
                            else
                            {
                                semanticErrors.add("Cannot begin expression with non-variable");
                                return;
                            }
                        }
                        else
                        {
                            semanticErrors.add("Undeclared identifier \"" + varId + "\".");
                            myAttributes.put("type", null);
                            return;
                        }

                        TypeSymbol type2 = (TypeSymbol)exprOrFuncEndAttributes.get("type");
                        TypeSymbol resultType = inferType(type1, type2);

                        if(resultType != null)
                        {
                            myAttributes.put("type", resultType);
                        }
                        else
                        {
                            semanticErrors.add("Operation between incompatible types: \"" + type1 + "\" and \"" + type2 + "\"");
                            return;
                        }
                    }
                }
                //<STAT_ASSIGN_RHS> -> LPAREN <EXPR> RPAREN <PRIME_TERM>
                else if(children.get(0).getNodeType() == Token.LPAREN)
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    Map<String, Object> primeTermAttributes = attributes.get(children.get(3));

                    TypeSymbol type1 = (TypeSymbol)exprAttributes.get("type");
                    TypeSymbol type2 = (TypeSymbol)exprAttributes.get("type");
                    TypeSymbol resultType = inferType(type1, type2);

                    if(resultType != null)
                    {
                        myAttributes.put("type", resultType);
                    }
                    else
                    {
                        semanticErrors.add("Operation between incompatible types: \"" + type1 + "\" and \"" + type2 + "\"");
                        return;
                    }
                }
                //<STAT_ASSIGN_RHS> -> <CONST> <PRIME_TERM>
                else if(children.get(0).getNodeType() == NonterminalSymbol.CONST)
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
            {

            }

            case "FUNC_CALL_END":
            case "IF_STAT":
            case "IF_END":

            case "CONST":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                analyze(children.get(0));

                Map<String, Object> childAttributes = attributes.get(children.get(0));

                myAttributes.put("type", childAttributes.get("type"));

            } break;


            case "FACTOR":



            case "EXPR":
            case "TERM1":
            case "TERM2":
            case "TERM3":
            case "TERM4":
            case "TERM5":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }

                Map<String, Object> term1Attributes = attributes.get(children.get(0));
                Map<String, Object> term2Attributes = attributes.get(children.get(1));

                TypeSymbol type1 = (TypeSymbol)term1Attributes.get("type");
                TypeSymbol type2 = (TypeSymbol)term2Attributes.get("type");
                TypeSymbol resultType = inferType(type1, type2);

                if(resultType != null)
                {
                    myAttributes.put("type", resultType);
                }
                else
                {
                    semanticErrors.add("Operation between incompatible types: \"" + type1 + "\" and \"" + type2 + "\"");
                    return;
                }
            } break;


            //Since these two are basically the same (except tail starts with a comma), share the code between them.
            //Tail will simply get an offset term of 1 when retrieving child nodes to let it "skip" the comma node
            case "EXPR_LIST":
            case "EXPR_LIST_TAIL":
            {
                int offset = 0;

                if(nodeTypeStr.equals("EXPR_LIST_TAIL"))
                    offset = 1;

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

                //<EXPR_LIST (TAIL)> -> NULL
                if(children.isEmpty())
                {
                    myAttributes.put("typeList", new ArrayList<TypeSymbol>());
                }
                //<EXPR_LIST (TAIL)> -> (COMMA) <EXPR> <EXPR_LIST_TAIL>
                else
                {
                    List<TypeSymbol> typeList = new ArrayList<TypeSymbol>();

                    Map<String, Object> exprAttributes = attributes.get(children.get(0 + offset));
                    typeList.add((TypeSymbol)exprAttributes.get("type"));

                    Map<String, Object> exprListTailAttributes = attributes.get(children.get(1 + offset));
                    typeList.addAll((List<TypeSymbol>)exprListTailAttributes.get("typeList"));

                    myAttributes.put("typeList", typeList);
                }

            } break;

            case "LVALUE_TAIL":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    analyze(child);
                }

                //<LVALUE_TAIL> -> NULL
                if(children.isEmpty())
                {
                    return;
                }
                //<LVALUE_TAIL> -> LBRACK <EXPR> RBRACK
                else
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    TypeSymbol exprType = (TypeSymbol)exprAttributes.get("type");

                    myAttributes.put("type", exprType);

                    if(exprType != TypeSymbol.INT)
                    {
                        semanticErrors.add("Arrays must be indexed by type \"int\".");
                        return;
                    }
                }

                Map<String, Object> childAttributes = attributes.get(children.get(0));

                myAttributes.put("type", childAttributes.get("type"));
            }

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

            default:
            {
                Output.println("Internal compiler error. Could not find grammar symbol " + nodeTypeStr + " to semantically analyze.");
                System.exit(-1);
            }
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
