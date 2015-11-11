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

import java.util.*;

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
     * - Inherit necessary attributes from parent and assign known attributes to self
     * - Recursively analyze children nodes
     * - Ensure children nodes are compatible types (when necessary) and follow proper semantics. Assign additional
     * attributes to self that are dependent on children node attributes.
     *
     * @param node
     */
    @SuppressWarnings("unchecked") //the attributes are basically just dynamically typed variables and we cast the crap out of them
    private void analyze(ParseTreeNode node)
    {
        //since we are using an empty GrammarSymbol interface to store both Token enums and NonterminalSymbol enums
        //in the same collections, we can't really switch on the kind of GrammarSymbol. Instead, we can cast the
        //GrammarSymbol to the type it really is, and get the String name of it's value, then switch on that.
        String nodeTypeStr = "";
        GrammarSymbol nodeType = node.getNodeType();

        if (nodeType instanceof Token)
        {
            nodeTypeStr = ((Token) nodeType).toString();
        }
        else if (nodeType instanceof NonterminalSymbol)
        {
            nodeTypeStr = ((NonterminalSymbol) nodeType).toString();
        }
        else
        {
            Output.println("Internal compiler error: Compiler failed to cast parse tree node type to either a token or nonterminal." +
                    "Perhaps you put semantic actions in your parse tree?");
            System.exit(-1);
        }

        //since PRIME_TERM will behave however the case it expands to behaves, just change the string
        //before doing the switch.
        if (nodeTypeStr.equals("PRIME_TERM"))
        {
            List<ParseTreeNode> children = node.getChildren();

            if (!children.isEmpty())
            {
                GrammarSymbol childSymbol = children.get(0).getNodeType();

                if (childSymbol == Token.OR)
                    nodeTypeStr = "EXPR_PRIME";
                else if (childSymbol == Token.AND)
                    nodeTypeStr = "TERM1_PRIME";
                else if (childSymbol == NonterminalSymbol.INEQUALITY_OP)
                    nodeTypeStr = "TERM2_PRIME";
                else if (childSymbol == NonterminalSymbol.EQUALITY_OP)
                    nodeTypeStr = "TERM3_PRIME";
                else if (childSymbol == NonterminalSymbol.ADD_SUB_OP)
                    nodeTypeStr = "TERM4_PRIME";
                else if (childSymbol == NonterminalSymbol.MUL_DIV_OP)
                    nodeTypeStr = "TERM5_PRIME";
            }
        }

        //I'm 100% sure this could be made more elegant using polymorphism, but lets get it working first.

        switch (nodeTypeStr)
        {
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

            case "INT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);
                myAttributes.put("type", TypeSymbol.INT);
            }
            break;

            case "FLOAT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);
                myAttributes.put("type", TypeSymbol.FLOAT);
            }
            break;

            case "ID":
            {
                //ID's can decorate their own node, but they don't perform any real semantic actions

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //inherit function symbol table from parent
                ParseTreeNode parent = node.getParent();
                Map<String, Object> parentAttributes = attributes.get(parent);
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                String functionName = (String)parentAttributes.get("functionName");

                myAttributes.put("functionSymbolTable", functionSymbolTable);
                myAttributes.put("functionName", functionName);

                Symbol symbol;
                String id = node.getLiteralToken();

                //check function symbol table first
                if (functionSymbolTable != null && functionSymbolTable.containsKey(id))
                {
                    symbol = functionSymbolTable.get(id);
                }
                else if (globalSymbolTable.containsKey(id))
                {
                    symbol = globalSymbolTable.get(id);
                }
                else
                {
                    addSemanticError("Undeclared identifier \"" + id + "\"", node.getLineNumber());
                    return;
                }

                TypeSymbol myType;

                if (symbol instanceof VariableSymbol)
                    myType = ((VariableSymbol) symbol).getType();
                else if (symbol instanceof FunctionSymbol)
                    myType = ((FunctionSymbol) symbol).getReturnType();
                else
                    myType = ((TypeSymbol) symbol);

                myAttributes.put("type", myType);
            }
            break;

            case "INTLIT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                myAttributes.put("type", TypeSymbol.INT);
                attributes.put(node, myAttributes);
            }
            break;

            case "FLOATLIT":
            {
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
            case "ID_LIST":
            case "ID_LIST_TAIL":
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

                if (node.getParent() != null)
                {
                    //inherit function symbol table (useless for most. some, like PARAM, use it)
                    Map<String, Object> parentAttributes = attributes.get(node.getParent());
                    Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");

                    //null outside function decl, but we won't ever reference it outside functions, so no harm done
                    String functionName = (String) parentAttributes.get("functionName");

                    myAttributes.put("functionSymbolTable", functionSymbolTable);
                    myAttributes.put("functionName", functionName);
                }

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

            }
            break;


            case "FUNC_DECLARATION":
            {
                //<FUNC_DECLARATION> -> FUNCTION ID LPAREN <PARAM_LIST> RPAREN <RET_TYPE> BEGIN <STAT_SEQ> END SEMI


                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();

                for (ParseTreeNode child : children)
                {
                    analyze(child);

                    //we have to bind the function table to this node
                    //BEFORE analyzing all of the children, because
                    //the STAT_SEQ will try to inherit it
                    if (child == children.get(1))
                    {
                        //Assign correct function symbol table to this node, so other children node can inherit it
                        String funcId = children.get(1).getLiteralToken();

                        if (globalSymbolTable.containsKey(funcId))
                        {
                            //failing this cast would be an internal compiler error, but i am confident it can't
                            //happen so I'm not logging it
                            FunctionSymbol func = (FunctionSymbol) globalSymbolTable.get(funcId);
                            myAttributes.put("functionSymbolTable", func.getSymbolTable());
                            myAttributes.put("functionName", func.getName());
                        }
                        else
                        {
                            Output.println("Internal compiler error: Could not find function \"" + funcId + "\" in symbol table" +
                                    " despite receiving it's name from a function declaration in the parse tree.");
                            System.exit(-1);
                        }
                    }
                }


            }
            break;


            case "VAR_DECLARATION":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                //<VAR_DECLARATION> -> VAR <ID_LIST> COLON <TYPE_SYMBOL> <OPTIONAL_INIT>  SEMI
                Map<String, Object> typeSymbolAttributes = attributes.get(children.get(3));
                TypeSymbol type = (TypeSymbol) typeSymbolAttributes.get("type");

                Map<String, Object> optionalInitAttributes = attributes.get(children.get(4));
                TypeSymbol initType = (TypeSymbol) optionalInitAttributes.get("type");

                if (!isTypeCompatibleInit(type, initType))
                {
                    addSemanticError("Illegal initialization type.\nNote:Only first-order type derivations of int/float may be" +
                            " initialized with int/float constants.", node.getLineNumber());
                    return;
                }

                myAttributes.put("type", type);
            }
            break;


            case "TYPE_SYMBOL":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                //<TYPE_SYMBOL> -> ID
                //<TYPE_SYMBOL> <TYPE_ID>
                if (children.get(0).getNodeType() == Token.ID || children.get(0).getNodeType() == NonterminalSymbol.TYPE_ID)
                {
                    Map<String, Object> idAttributes = attributes.get(children.get(0));
                    TypeSymbol idType = (TypeSymbol) idAttributes.get("type");

                    myAttributes.put("type", idType);
                }
                //<TYPE_SYMBOL> -> ARRAY LBRACK INTLIT RBRACK OF <TYPE_ID>
                else if (children.get(0).getNodeType() == Token.ARRAY)
                {
                    Map<String, Object> typeIdAttributes = attributes.get(children.get(5));
                    TypeSymbol type = (TypeSymbol) typeIdAttributes.get("type");

                    myAttributes.put("type", type);
                }
            }
            break;

            case "OPTIONAL_INIT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                //<OPTIONAL_INIT> -> ASSIGN <CONST>
                if (!children.isEmpty())
                {
                    Map<String, Object> constAttributes = attributes.get(children.get(1));
                    TypeSymbol constType = (TypeSymbol) constAttributes.get("type");

                    myAttributes.put("type", constType);
                }

            }
            break;

            case "STAT":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");

                //null outside function decl, but we won't ever reference it outside functions, so no harm done
                String functionName = (String) parentAttributes.get("functionName");

                myAttributes.put("functionSymbolTable", functionSymbolTable);
                myAttributes.put("functionName", functionName);


                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();

                for (ParseTreeNode child : children)
                {
                    analyze(child);

                    //after analyzing first child in STAT -> ID <STAT_ASSIGN_OR_FUNC> SEMI
                    //store the id's type
                    if (child == children.get(0) && child.getNodeType() == Token.ID)
                    {
                        Map<String, Object> idAttributes = attributes.get(children.get(0));
                        TypeSymbol idType = (TypeSymbol) idAttributes.get("type");

                        myAttributes.put("type", idType);
                    }
                }


                //<STAT> -> ID <STAT_ASSIGN_OR_FUNC> SEMI
                if (children.get(0).getNodeType() == Token.ID)
                {
                    ParseTreeNode statAssignOrFuncNode = children.get(1);
                    Map<String, Object> statAssignOrFuncAttributes = attributes.get(statAssignOrFuncNode);

                    if (!(boolean) statAssignOrFuncAttributes.get("isAssignment"))
                    {
                        //STAT_ASSIGN_OR_FUNC is a function
                        List<TypeSymbol> arguments = (List<TypeSymbol>) statAssignOrFuncAttributes.get("typeList");
                        String funcId = children.get(0).getLiteralToken();
                        Symbol symbol = globalSymbolTable.get(funcId);

                        if (!(symbol instanceof FunctionSymbol))
                        {
                            addSemanticError("Cannot call \"" + funcId + "\" as if it were a function.", node.getLineNumber());
                            return;
                        }
                    }
                }
                //<STAT> -> RETURN <EXPR> SEMI
                else if (children.get(0).getNodeType() == Token.RETURN)
                {
                    if(functionName == null)
                    {
                        addSemanticError("Return statement cannot exist outside of function declaration.", node.getLineNumber());
                        return;
                    }
                    FunctionSymbol functionSymbol = (FunctionSymbol) globalSymbolTable.get(functionName);
                    TypeSymbol expectedReturnType = functionSymbol.getReturnType();

                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    TypeSymbol actualReturnType = (TypeSymbol) exprAttributes.get("type");

                    if (!isTypeCompatibleAssignment(expectedReturnType, actualReturnType))
                    {
                        if (expectedReturnType == null)
                        {
                            addSemanticError("Cannot return value in void function \"" + functionName + "\".", node.getLineNumber());
                            return;
                        }
                        else
                        {
                            addSemanticError("Function \"" + functionName + "\" must return type \"" + expectedReturnType.getName() + "\".", node.getLineNumber());
                            return;
                        }
                    }
                }
                //<STAT> -> WHILE <EXPR> DO <STAT_SEQ> ENDDO SEMI
                else if (children.get(0).getNodeType() == Token.WHILE)
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    TypeSymbol exprType = (TypeSymbol) exprAttributes.get("type");

                    if (exprType != TypeSymbol.INT)
                    {
                        addSemanticError("While loop condition must resolve to type int.", node.getLineNumber());
                        return;
                    }
                }
                //<STAT> -> FOR ID ASSIGN <EXPR> TO <EXPR> DO <STAT_SEQ> ENDDO SEMI
                else if (children.get(0).getNodeType() == Token.FOR)
                {
                    Map<String, Object> idAttributes = attributes.get(children.get(1));
                    TypeSymbol idType = (TypeSymbol) idAttributes.get("type");

                    Map<String, Object> expr1Attributes = attributes.get(children.get(3));
                    TypeSymbol expr1Type = (TypeSymbol) expr1Attributes.get("type");

                    Map<String, Object> expr2Attributes = attributes.get(children.get(5));
                    TypeSymbol expr2Type = (TypeSymbol) expr2Attributes.get("type");

                    if (idType != TypeSymbol.INT)
                    {
                        addSemanticError("For loop variable must be type int.", node.getLineNumber());
                        return;
                    }

                    if (expr1Type != TypeSymbol.INT || expr2Type != TypeSymbol.INT)
                    {
                        addSemanticError("For loop upper and lower bound expressions must resolve to int.", node.getLineNumber());
                        return;
                    }
                }
                //<STAT> -> BREAK SEMI
                //<STAT> -> <IF_STAT_IF_END> SEMI
                else
                {

                }
            }
            break;

            case "STAT_ASSIGN_OR_FUNC":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }


                //Semantic check results of children analyses, and add remaining attributes to self

                //<STAT_ASSIGN_OR_FUNC> -> <LVALUE_TAIL> ASSIGN <STAT_ASSIGN_RHS>
                if (children.get(0).getNodeType() == NonterminalSymbol.LVALUE_TAIL)
                {
                    myAttributes.put("isAssignment", true);

                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(0));
                    TypeSymbol myType = (TypeSymbol) parentAttributes.get("type");

                    boolean indexedIntoArray = !(boolean) lValueTailAttributes.get("isNull");

                    if (indexedIntoArray && !myType.isArrayOfDerivedType())
                    {
                        addSemanticError("Cannot index into variables whose type is not an array.", node.getLineNumber());
                        return;
                    }

                    if (indexedIntoArray)
                    {
                        myType = myType.derivedType();
                        //eg, the type of x[5] would be int in the following code
                        /**
                         * type ArrayInt = array [100] of int;
                         * var x : ArrayInt;
                         */
                    }

                    myAttributes.put("type", myType);

                    Map<String, Object> statAssignRhsAttributes = attributes.get(children.get(2));
                    TypeSymbol statAssignRhsType = (TypeSymbol) statAssignRhsAttributes.get("type");

                    if (!isTypeCompatibleAssignment(myType, statAssignRhsType))
                    {
                        //if type IS null, it means its an unknown identifier, which was reported elsewhere
                        if (myType != null && statAssignRhsType != null)
                        {
                            addSemanticError("Cannot assign type " + statAssignRhsType.getName() + " to variable of type " + myType.getName(), node.getLineNumber());
                        }
                    }
                }
                //<STAT_ASSIGN_OR_FUNC> -><FUNC_CALL_END>
                else
                {
                    myAttributes.put("isAssignment", false);

                    Map<String, Object> funcCallEndAttributes = attributes.get(children.get(0));
                    List<TypeSymbol> typeList = (List<TypeSymbol>) funcCallEndAttributes.get("typeList");
                    myAttributes.put("typeList", typeList);
                }
            }
            break;

            case "STAT_ASSIGN_RHS":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //need to store results of some children before analyzing future children,
                //so defer child analysis to the individual cases STAT_ASSIGN_RHS can expand to
                List<ParseTreeNode> children = node.getChildren();


                //Semantic check results of children analyses, and add remaining attributes to self
                //<STAT_ASSIGN_RHS> -> ID <EXPR_OR_FUNC_END>
                if (children.get(0).getNodeType() == Token.ID)
                {
                    analyze(children.get(0));
                    Map<String, Object> idAttributes = attributes.get(children.get(0));
                    TypeSymbol idType = (TypeSymbol) idAttributes.get("type");
                    myAttributes.put("type", idType);

                    analyze(children.get(1));

                    Map<String, Object> exprOrFuncEndAttributes = attributes.get(children.get(1));

                    if ((boolean) exprOrFuncEndAttributes.get("isFunction"))
                    {
                        //RHS is a function call.

                        String funcId = children.get(0).getLiteralToken();

                        if (!globalSymbolTable.containsKey(funcId))
                        {
                            addSemanticError("Undeclared identifier \"" + funcId + "\".", node.getLineNumber());
                            myAttributes.put("type", null);
                            return;
                        }

                        //double check that the id they gave us was, in fact, a function
                        Symbol funcSymbol = globalSymbolTable.get(funcId);

                        if (!(funcSymbol instanceof FunctionSymbol))
                        {
                            addSemanticError("\"" + funcId + "\" is not a function.", node.getLineNumber());
                            myAttributes.put("type", null);
                            return;
                        }

                        FunctionSymbol function = (FunctionSymbol) funcSymbol;

                        if(function.getReturnType() == null)
                        {
                            addSemanticError("Void function \"" + funcId + "\" cannot be RHS of assignment.", node.getLineNumber());
                            myAttributes.put("type", null);
                            return;
                        }

                        myAttributes.put("type", function.getReturnType());

                        List<TypeSymbol> paramTypes = (List<TypeSymbol>) exprOrFuncEndAttributes.get("typeList"); //parameters
                        verifyFunctionParameters(function, paramTypes, node.getLineNumber());
                    }
                    else
                    {
                        //RHS is an expression

                        //update type based on expression
                        myAttributes.put("type", exprOrFuncEndAttributes.get("type"));

                        String varId = children.get(0).getLiteralToken();

                        if (functionSymbolTable != null && functionSymbolTable.containsKey(varId))
                        {
                            //all good
                        }
                        else if (globalSymbolTable.containsKey(varId))
                        {
                            Symbol symbol = globalSymbolTable.get(varId);

                            if (symbol instanceof VariableSymbol)
                            {
                                //all good
                            }
                            else
                            {
                                addSemanticError("Cannot begin expression with non-variable", node.getLineNumber());
                                return;
                            }
                        }
                        else
                        {
                            //error gets printed elsewhere
                            myAttributes.put("type", null);
                            return;
                        }
                    }
                }
                //<STAT_ASSIGN_RHS> -> LPAREN <EXPR> RPAREN <PRIME_TERM>
                else if (children.get(0).getNodeType() == Token.LPAREN)
                {
                    analyze(children.get(0));
                    analyze(children.get(1));
                    analyze(children.get(2));

                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    TypeSymbol myType = (TypeSymbol) exprAttributes.get("type");

                    myAttributes.put("type", myType);

                    analyze(children.get(3));
                    Map<String, Object> primeTermAttributes = attributes.get(children.get(3));
                    TypeSymbol primeTermType = (TypeSymbol) primeTermAttributes.get("type");

                    TypeSymbol resultType = inferType(myType, primeTermType);

                    if (resultType != null)
                    {
                        myAttributes.put("type", resultType);
                    }
                    else
                    {
                        addSemanticError("Operation between incompatible types: \"" + myType.getName() +
                                "\" and \"" + primeTermType.getName() + "\"", node.getLineNumber());
                        return;
                    }
                }
                //<STAT_ASSIGN_RHS> -> <CONST> <PRIME_TERM>
                else if (children.get(0).getNodeType() == NonterminalSymbol.CONST)
                {
                    analyze(children.get(0));
                    Map<String, Object> constAttributes = attributes.get(children.get(0));
                    TypeSymbol myType = (TypeSymbol) constAttributes.get("type");

                    myAttributes.put("type", myType);

                    analyze(children.get(1));

                    Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                    TypeSymbol primeTermType = (TypeSymbol) primeTermAttributes.get("type");

                    TypeSymbol resultType = inferType(myType, primeTermType);

                    if (resultType != null)
                    {
                        myAttributes.put("type", resultType);
                    }
                    else
                    {
                        addSemanticError("Operation between incompatible types: \"" + myType.getName() +
                                "\" and \"" + primeTermType.getName() + "\"", node.getLineNumber());
                        return;
                    }
                }
            }
            break;

            case "EXPR_OR_FUNC_END":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                List<ParseTreeNode> children = node.getChildren();

                //let each case control analysis, so they can store intermediate attributes

                //<EXPR_OR_FUNC_END> -> <LVALUE_TAIL> <PRIME_TERM>
                if (children.get(0).getNodeType() == NonterminalSymbol.LVALUE_TAIL)
                {
                    myAttributes.put("isFunction", false);
                    TypeSymbol myType = (TypeSymbol) parentAttributes.get("type");

                    analyze(children.get(0));

                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(0));
                    boolean indexedIntoArray = !(boolean) lValueTailAttributes.get("isNull");

                    if (indexedIntoArray && !myType.isArrayOfDerivedType())
                    {
                        addSemanticError("Cannot index into variables whose type is not an array.", node.getLineNumber());
                        return;
                    }

                    if (indexedIntoArray)
                    {
                        myType = myType.derivedType();
                        //eg, the type of x[5] would be int in the following code
                        /**
                         * type ArrayInt = array [100] of int;
                         * var x : ArrayInt;
                         */
                    }

                    myAttributes.put("type", myType);

                    analyze(children.get(1));

                    Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                    TypeSymbol primeTermType = (TypeSymbol) primeTermAttributes.get("type");

                    myType = inferType(myType, primeTermType);
                    myAttributes.put("type", myType);
                }
                //<EXPR_OR_FUNC_END> -> <FUNC_CALL_END>
                else
                {
                    for (ParseTreeNode child : children)
                    {
                        analyze(child);
                    }

                    Map<String, Object> funcCallEndAttributes = attributes.get(children.get(0));

                    myAttributes.put("isFunction", true);
                    myAttributes.put("typeList", funcCallEndAttributes.get("typeList"));
                }

            }
            break;

            case "FUNC_CALL_END":
            {
                //<FUNC_CALL_END> -> LPAREN <EXPR_LIST> RPAREN

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                Map<String, Object> exprListAttributes = attributes.get(children.get(1));

                myAttributes.put("typeList", exprListAttributes.get("typeList"));

            }
            break;

            case "IF_STAT":
            {
                //<IF_STAT> -> IF <EXPR> THEN <STAT_SEQ>

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                Map<String, Object> exprAttributes = attributes.get(children.get(1));
                TypeSymbol exprType = (TypeSymbol) exprAttributes.get("type");

                if (exprType != TypeSymbol.INT)
                {
                    addSemanticError("If statement condition must resolve to type int.", node.getLineNumber());
                    return;
                }
            }
            break;

            case "IF_END":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }
            }
            break;

            case "TYPE_ID":
            case "CONST":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                analyze(children.get(0));

                Map<String, Object> childAttributes = attributes.get(children.get(0));

                myAttributes.put("type", childAttributes.get("type"));

            }
            break;


            case "FACTOR":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                //<FACTOR> -> LPAREN <EXPR> RPAREN
                if (children.get(0).getNodeType() == Token.LPAREN)
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    TypeSymbol exprType = (TypeSymbol) exprAttributes.get("type");

                    myAttributes.put("type", exprType);
                }
                //<FACTOR> -> ID <LVALUE_TAIL>
                else if (children.get(0).getNodeType() == Token.ID)
                {
                    String varId = children.get(0).getLiteralToken();
                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(1));

                    boolean indexedIntoArray = !(boolean) lValueTailAttributes.get("isNull");
                    Symbol symbol;

                    if (functionSymbolTable != null && functionSymbolTable.containsKey(varId))
                    {
                        //FST should only contain parameters, so we can assume this cast is safe
                        symbol = (VariableSymbol) functionSymbolTable.get(varId);
                    }
                    else if (globalSymbolTable.containsKey(varId))
                    {
                        symbol = globalSymbolTable.get(varId);
                    }
                    else
                    {
                        //error already reported
                        //addSemanticError("Undeclared identifier \"" + varId + "\"", node.getLineNumber());
                        return;
                    }

                    if (symbol instanceof VariableSymbol)
                    {
                        TypeSymbol type = ((VariableSymbol) symbol).getType();

                        if (indexedIntoArray && !type.isArrayOfDerivedType())
                        {
                            addSemanticError("Cannot index into variables whose type is not an array.", node.getLineNumber());
                            return;
                        }

                        if (indexedIntoArray)
                        {
                            type = type.derivedType();
                            //eg, the type of x[5] would be int in the following code
                            /**
                             * type ArrayInt = array [100] of int;
                             * var x : ArrayInt;
                             */
                        }

                        myAttributes.put("type", type);
                    }
                    else
                    {
                        addSemanticError("Expressions can only include variables and constants.", node.getLineNumber());
                        return;
                    }

                }
                //<FACTOR> -> <CONST>
                else if (children.get(0).getNodeType() == NonterminalSymbol.CONST)
                {
                    Map<String, Object> constAttributes = attributes.get(children.get(0));
                    TypeSymbol exprType = (TypeSymbol) constAttributes.get("type");

                    myAttributes.put("type", exprType);
                }
            }
            break;


            //Since these four are basically the same (except tails starts with a comma), share the code between them.
            //Tails will simply get an offset term of 1 when retrieving child nodes to let it "skip" the comma node
            case "EXPR_LIST":
            case "EXPR_LIST_TAIL":
            case "PARAM_LIST":
            case "PARAM_LIST_TAIL":
            {
                int offset = 0;

                if (nodeTypeStr.endsWith("TAIL"))
                    offset = 1;

                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                //<EXPR_LIST (TAIL)> -> NULL
                //<PARAM_LIST (TAIL)> -> NULL
                if (children.isEmpty())
                {
                    myAttributes.put("typeList", new ArrayList<TypeSymbol>());
                }
                //<EXPR_LIST (TAIL)> -> (COMMA) <EXPR> <EXPR_LIST_TAIL>
                //<PARAM_LIST (TAIL)> -> (COMMA) <PARAM> <PARAM_LIST_TAIL>
                else
                {
                    List<TypeSymbol> typeList = new ArrayList<>();

                    Map<String, Object> exprAttributes = attributes.get(children.get(0 + offset));
                    typeList.add((TypeSymbol) exprAttributes.get("type"));

                    Map<String, Object> exprListTailAttributes = attributes.get(children.get(1 + offset));
                    typeList.addAll((List<TypeSymbol>) exprListTailAttributes.get("typeList"));

                    myAttributes.put("typeList", typeList);
                }

            }
            break;

            case "LVALUE_TAIL":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                {
                    analyze(child);
                }

                //<LVALUE_TAIL> -> NULL
                if (children.isEmpty())
                {
                    myAttributes.put("isNull", true);
                    return;
                }
                //<LVALUE_TAIL> -> LBRACK <EXPR> RBRACK
                else
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    TypeSymbol exprType = (TypeSymbol) exprAttributes.get("type");

                    myAttributes.put("type", exprType);
                    myAttributes.put("isNull", false);

                    if (exprType != TypeSymbol.INT)
                    {
                        addSemanticError("Arrays must be indexed by type \"int\".", node.getLineNumber());
                        return;
                    }
                }
            }
            break;

            case "EXPR":
            case "TERM1":
            case "TERM2":
            case "TERM3":
            case "TERM4":
            case "TERM5":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                //Analyze children node
                List<ParseTreeNode> children = node.getChildren();

                analyze(children.get(0));
                Map<String, Object> termAttributes = attributes.get(children.get(0));
                TypeSymbol myType = (TypeSymbol) termAttributes.get("type");

                myAttributes.put("type", myType);

                analyze(children.get(1));
                Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                TypeSymbol primeTermType = (TypeSymbol) primeTermAttributes.get("type");

                //since prime term used myType when determining its type, we can
                //just grab the resulting type (unless the prime term was empty)
                if (primeTermType != null)
                {
                    myAttributes.put("type", primeTermType);
                }
            }
            break;

            case "PRIME_TERM": //NOTE: PRIME_TERM actually gets mapped to whichever prime term it is (unless it is null)
            case "EXPR_PRIME":
            case "TERM1_PRIME":
            case "TERM2_PRIME":
            case "TERM3_PRIME":
            case "TERM4_PRIME":
            case "TERM5_PRIME":
            {
                //Assign some attributes to self
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>) parentAttributes.get("functionSymbolTable");
                myAttributes.put("functionSymbolTable", functionSymbolTable);

                String functionName = (String)parentAttributes.get("functionName");
                myAttributes.put("functionName", functionName);

                TypeSymbol leftType = (TypeSymbol) parentAttributes.get("type");

                List<ParseTreeNode> children = node.getChildren();
                if (children.isEmpty())
                {
                    myAttributes.put("type", null);
                    return;
                }

                //Analyze children node
                analyze(children.get(0)); //analyze operator
                analyze(children.get(1)); //analyze right operand

                //Don't analyze the last child yet. First, resolve our own type based on the
                //type of our parent (left operand) and the type of our right operand. Once we do this,
                //the last child will be able to use OUR type as its left operand. We can then update
                //our final type after analyzin the last child

                Map<String, Object> rightOperandAttributes = attributes.get(children.get(1));
                TypeSymbol rightType = (TypeSymbol) rightOperandAttributes.get("type");

                TypeSymbol myType = inferType(leftType, rightType);

                if (myType != null)
                {

                    // +, -, *, /
                    if (nodeTypeStr.contains("4") || nodeTypeStr.contains("5"))
                    {
                        //no problems
                    }
                    // =, <>
                    else if (nodeTypeStr.contains("3"))
                    {
                        if (leftType == null || rightType == null)
                            return;

                        if (!leftType.getName().equals(rightType.getName()))
                        {
                            addSemanticError("= or <> require operands of the same type.", node.getLineNumber());
                            return;
                        }

                        myType = TypeSymbol.INT; //comparisons always return 0, 1
                    }
                    // <, <=, >, >=
                    else if (nodeTypeStr.contains("2"))
                    {
                        if (leftType == null || rightType == null)
                            return;

                        if (!(leftType == rightType && (leftType == TypeSymbol.INT || rightType == TypeSymbol.FLOAT)))
                        {
                            addSemanticError("<, <=, >, >= require operands that are both ints or both floats.", node.getLineNumber());
                            return;
                        }

                        myType = TypeSymbol.INT; //comparisons always return 0, 1
                    }
                    //AND, OR
                    else
                    {
                        if (myType != TypeSymbol.INT)
                        {
                            addSemanticError("&, | require operands that are both ints.", node.getLineNumber());
                            return;
                        }
                    }

                    myAttributes.put("type", myType);
                }
                else
                {
                    if (leftType != null && rightType != null)
                    {
                        addSemanticError("Operation between incompatible types: \"" + leftType.getName() + "\" and \"" + rightType.getName() + "\"", node.getLineNumber());
                        return;
                    }
                }

                //now that we've added a preliminary type to ourselves, go to the last child, which might use that type
                //to determine its own type. Then update our type accordingly.
                analyze(children.get(2));
                Map<String, Object> primeTermTailAttributes = attributes.get(children.get(2));

                TypeSymbol primeTermTailType = (TypeSymbol) primeTermTailAttributes.get("type");
                myType = inferType(myType, primeTermTailType);

                myAttributes.put("type", myType);
            }
            break;

            default:
            {
                Output.println("Internal compiler error. Could not find grammar symbol " + nodeTypeStr + " to semantically analyze.");
                System.exit(-1);
            }
        }
    }

    public Map<ParseTreeNode, Map<String, Object>> getParseTreeAttributes()
    {
        return attributes;
    }

    private boolean isTypeCompatibleAssignment(TypeSymbol lhsType, TypeSymbol rhsType)
    {
        return (lhsType == TypeSymbol.FLOAT && rhsType == TypeSymbol.INT) || lhsType == rhsType;
    }

    private boolean isTypeCompatibleInit(TypeSymbol lhsType, TypeSymbol rhsType)
    {
        if(rhsType == null) //no init is fine
            return true;

        if(isTypeCompatibleAssignment(lhsType, rhsType))
            return true;

        if(lhsType.derivedType() == TypeSymbol.INT && rhsType == TypeSymbol.INT)
            return true;

        if(lhsType.derivedType() == TypeSymbol.FLOAT && (rhsType == TypeSymbol.FLOAT || rhsType == TypeSymbol.INT))
            return true;

        return false;
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
        if (type2 == null)
            return type1;

        if (type1 == type2)
            return type1;

        if ((type1 == TypeSymbol.INT && type2 == TypeSymbol.FLOAT) || (type1 == TypeSymbol.FLOAT && type2 == TypeSymbol.INT))
            return TypeSymbol.FLOAT;

        return null;
    }

    /**
     * Verifies that the parameters passed in match up to the order and type of the parameters
     * of the function symbol. On success, does nothing. On failure, adds errors to the semantic
     * error list.
     */
    private void verifyFunctionParameters(FunctionSymbol function, List<TypeSymbol> actualParameters, int lineNumber)
    {
        List<TypeSymbol> expectedParameters = function.getParameterList();

        //before doing any checking, lets turn the lists to strings so that on error
        //we have nicely formatted strings to print out
        String expectedParametersStr = paramListToString(expectedParameters);
        String actualParametersStr = paramListToString(actualParameters);

        if (actualParameters.size() != expectedParameters.size())
        {
            addSemanticError("Cannot pass " + actualParameters.size() + " arguments to function \"" +
                    function.getName() + "\" expecting arguments " + expectedParametersStr, lineNumber);
            return;
        }

        for (int i = 0; i < expectedParameters.size(); i++)
        {
            //this allows for ints to be used in place of floats
            if (!isTypeCompatibleAssignment(expectedParameters.get(i), actualParameters.get(i)))
            {
                addSemanticError("Cannot pass arguments " + actualParametersStr + " to function \"" +
                        function.getName() + "\" expecting arguments " + expectedParametersStr, lineNumber);
                return;
            }
        }
    }

    /**
     * Turns list of parameters into a nicely formatted string, such as
     * "(int, ArrayInt, float, int)"
     * Useful for printing error messages.
     *
     * @param parameters
     * @return
     */
    private String paramListToString(List<TypeSymbol> parameters)
    {
        String paramStr = "(";
        for (TypeSymbol parameter : parameters)
        {
            paramStr += parameter.getName() + ", ";
        }

        if (paramStr.endsWith(", "))
            paramStr = paramStr.substring(0, paramStr.length() - 2);

        if (paramStr.length() == 1) //no parameters
            paramStr += "[void]";

        paramStr += ")";

        return paramStr;
    }

    private void addSemanticError(String error, int lineNumber)
    {
        semanticErrors.add("(semantic error): Line " + lineNumber + "\n" + error);
    }
}
