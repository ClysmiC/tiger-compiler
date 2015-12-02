package com.tiger.compiler.frontend.irgeneration;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 11/7/2015.
 */
public class TigerIrGenerator
{
    private ParseTreeNode parseTreeRoot;
    private Map<String, Symbol> globalSymbolTable;
    private Map<ParseTreeNode, Map<String, Object>> semanticAttributes;
    private Map<ParseTreeNode, Map<String, Object>> attributes; //for IR generation, such as register name

    //stores the actual code lines that get generated
    private List<String> code;

    private int nextTempRegister;
    private int nextIfLabel;
    private int nextForLabel;
    private int nextWhileLabel;
    private int nextEqualityOpLabel;
    private int nextInequalityOpLabel;

    //argument registers get numbered with this
    //when expr_list_tail goes to null, this number gets reset to 0,
    //since that function call is complete
    private int argumentNumber;

    public TigerIrGenerator(ParseTreeNode parseTreeRoot,
                            Map<ParseTreeNode, Map<String, Object>> parseTreeAttributes,
                            Map<String, Symbol> globalSymbolTable)
    {
        this.parseTreeRoot = parseTreeRoot;
        this.semanticAttributes = parseTreeAttributes;
        this.globalSymbolTable = globalSymbolTable;

        code = new LinkedList<String>();
        attributes = new HashMap<>();
        nextTempRegister = 0;
        nextIfLabel = 0;
        nextForLabel = 0;
        nextWhileLabel = 0;
        nextEqualityOpLabel = 0;
        nextInequalityOpLabel = 0;

        argumentNumber = 0;
    }

    public String[] generateCode()
    {
        generateCode(parseTreeRoot);
        return code.toArray(new String[code.size()]);
    }

    /**
     * Recursively walks the tree, generating the code for each line.
     *
     * @param node
     */
    @SuppressWarnings("unchecked") //the attributes are basically just dynamically typed variables and we cast the crap out of them
    private void generateCode(ParseTreeNode node)
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
            case "INT":
            case "FLOAT":
                return;

            case "ID":
            {
                //IDs are stored in registers named _[id-name]_type
                //If the ID is a parameter to a function, we don't want to overwrite any global variables with the
                //same name, so we store it in register named __[function-name]_[id-name]
                //two underscores are required because there could be a global variable named [function-name]_[id-name]
                //and we don't want to risk overriding that
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> mySemanticAttributes = semanticAttributes.get(node);
                String functionName = (String)mySemanticAttributes.get("functionName");

                myAttributes.put("functionName", functionName);
                TypeSymbol myType = (TypeSymbol)mySemanticAttributes.get("type");

                String idName = node.getLiteralToken();

                if(functionName == null) //not in a function
                {
                    if(myType != null && myType.baseType() == TypeSymbol.INT)
                    {
                        myAttributes.put("register", idName + "_int");
                    }
                    else
                    {
                        myAttributes.put("register", idName + "_float");
                    }
                }
                else
                {
                    Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>)mySemanticAttributes.get("functionSymbolTable");

                    Symbol param = functionSymbolTable.get(idName);

                    if(param == null) //it is a global variable, not a param
                    {
                        if(myType != null && myType.baseType() == TypeSymbol.INT)
                        {
                            myAttributes.put("register", idName + "_int");
                        }
                        else
                        {
                            myAttributes.put("register", idName + "_float");
                        }
                    }
                    else //it is a parameter
                    {
                        if(myType != null && myType.baseType() == TypeSymbol.INT)
                        {
                            myAttributes.put("register", "_" + functionName + "_" + idName + "_int");
                        }
                        else
                        {
                            myAttributes.put("register", "_" + functionName + "_" + idName + "_float");
                        }
                    }
                }
            } break;

            case "INTLIT":
            case "FLOATLIT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                String value = node.getLiteralToken();

                String register;

                if(nodeTypeStr.equals("INTLIT"))
                    register = "_i" + nextTempRegister++;
                else
                    register = "_f" + nextTempRegister++;

                myAttributes.put("register", register);
                code.add(instruction("assign", register, value, null));
            } break;


            /**************************
             * NONTERMINALS
             **************************/
            case "VAR_DECLARATION_LIST":
            case "FUNC_DECLARATION_LIST":
            case "TYPE_DECLARATION":
            case "RET_TYPE":
            case "TYPE_SYMBOL":
            case "INEQUALITY_OP":
            case "EQUALITY_OP":
            case "ADD_SUB_OP":
            case "MUL_DIV_OP":
            case "TYPE_ID":
            {
                List<ParseTreeNode> children = node.getChildren();

                for(ParseTreeNode child: children)
                    generateCode(child);
            } break;


            case "TYPE_DECLARATION_LIST":
            {
                //if we generated code for children, it would store any intlits (used in array declarations)
                //into buffers that never get used. so simply skip this part of the parse tree.
                return;
            }

            case "TIGER_PROGRAM":
            {
                List<ParseTreeNode> children = node.getChildren();

                generateCode(children.get(0)); //LET
                generateCode(children.get(1)); //<DECLARATION_SEGMENT>
                generateCode(children.get(2)); //IN

                code.add("#");
                code.add("#");
                code.add("##########Body of the actual program begins here##########");
                code.add("_program_start:");

                generateCode(children.get(3)); //<STAT_SEQ>
                generateCode(children.get(4)); //END
            } break;

            case "DECLARATION_SEGMENT":
            {
                List<ParseTreeNode> children = node.getChildren();
                generateCode(children.get(0));

                code.add("##########START OF IR##########");
                code.add("#Initialize variables, implicitly to 0, or explicitly to indicated value");
                generateCode(children.get(1));

                //after var declarations, jump to the main program code (don't want functions executing unless they're called)
                code.add(instruction("goto", "_program_start", null, null));
                code.add("#"); //prints blank line in debug mode

                code.add("#");
                code.add("#Define functions, with the same labels as their names in the symbol table.");
                generateCode(children.get(2));
            } break;

            case "VAR_DECLARATION":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                for (ParseTreeNode child : children)
                    generateCode(child);

                //<VAR_DECLARATION> -> VAR <ID_LIST> COLON <TYPE_SYMBOL> <OPTIONAL_INIT> SEMI

                Map<String, Object> idListAttributes = attributes.get(children.get(1));
                Map<String, Object> optionalInitAttributes = attributes.get(children.get(4));

                boolean explicitlyInitialized = (boolean) optionalInitAttributes.get("initialized");
                String valToAssign;

                if (explicitlyInitialized)
                {
                    valToAssign = (String)optionalInitAttributes.get("register");
                }
                else
                {
                    //all implicit initializations go to 0
                    valToAssign = "0";
                }

                List<String> idListRegisters = (List<String>)idListAttributes.get("registerList");

                Map<String, Object> mySemanticAttributes = semanticAttributes.get(node);
                TypeSymbol myType = (TypeSymbol)mySemanticAttributes.get("type");

                if(myType.isArrayOfDerivedType())
                {
                    //initialize every entry in the array

                    String arraySize = "" + myType.getArraySize();
                    for (String register : idListRegisters)
                    {
                        code.add(instruction("assign", register, arraySize, valToAssign));
                    }
                }
                else
                {
                    for (String register : idListRegisters)
                    {
                        code.add(instruction("assign", register, valToAssign, null));
                    }
                }
            } break;

            case "ID_LIST":
            case "ID_LIST_TAIL":
            {
                int offset = 0;
                if(nodeTypeStr.equals("ID_LIST_TAIL"))
                    offset = 1;

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                    generateCode(child);

                List<String> registerList = new ArrayList<>();

                //<ID_LIST_TAIL> -> NULL
                if(children.isEmpty())
                {
                    myAttributes.put("registerList", registerList);
                }
                //<ID_LIST> -> ID <ID_LIST_TAIL>
                //<ID_LIST_TAIL> -> COMMA ID <ID_LIST_TAIL>
                else
                {
                    Map<String, Object> idAttributes = attributes.get(children.get(0 + offset));
                    Map<String, Object> idListTailAttributes = attributes.get(children.get(1 + offset));

                    String idRegister = (String)idAttributes.get("register");
                    List<String> idListRegisters = (List<String>)idListTailAttributes.get("registerList");

                    registerList.add(idRegister);
                    registerList.addAll(idListRegisters);

                    myAttributes.put("registerList", registerList);
                }
            } break;

            case "OPTIONAL_INIT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                    generateCode(child);

                //<OPTIONAL_INIT> -> ASSIGN <CONST>
                if(!children.isEmpty())
                {
                    Map<String, Object> constAttributes = attributes.get(children.get(1));
                    String constLiteralString = (String)constAttributes.get("literalString");

                    myAttributes.put("register", constLiteralString);
                    myAttributes.put("initialized", true);
                }
                //<OPTIONAL_INIT> -> NULL
                else
                {
                    myAttributes.put("initialized", false);
                }
            } break;


            case "CONST":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    generateCode(child);
                }

                //<CONST> -> INTLIT
                //<CONST> -> FLOATLIT

                Map<String, Object> litAttributes = attributes.get(children.get(0));
                String litRegister = (String)litAttributes.get("register");
                myAttributes.put("register", litRegister);
                myAttributes.put("literalString", children.get(0).getLiteralToken());
            } break;

            case "FUNC_DECLARATION":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();

                generateCode(children.get(0)); //FUNCTION
                generateCode(children.get(1)); //ID

                String funcName = children.get(1).getLiteralToken();
                myAttributes.put("functionBeingDeclared", funcName);
                code.add("#");
                code.add("" + funcName + ":");

                generateCode(children.get(2)); //LPAREN
                generateCode(children.get(3)); //<PARAM_LIST>

                generateCode(children.get(4)); //RPAREN
                generateCode(children.get(5)); //<RET_TYPE>
                generateCode(children.get(6)); //BEGIN


                generateCode(children.get(7)); //<STAT_SEQ>
                generateCode(children.get(8)); //END
                generateCode(children.get(9)); //SEMI

                FunctionSymbol function = (FunctionSymbol)globalSymbolTable.get(funcName);
                TypeSymbol returnType = function.getReturnType();

                //void functions don't have return statements in source code, so throw a return
                //at the bottom of the function decl
                if(returnType == null)
                {
                    code.add("\t#Void functions don't have return statements in source code.");
                    code.add("\t#Generate return instruction to return control back to call site.");
                    code.add(instruction("return", null, null, null));
                }
            } break;


            case "STAT":
            {
                code.add("#");
                code.add("\t#Statement on line " + node.getLineNumber() + " of source code.");

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String breakLabel = (String)parentAttributes.get("breakLabel");
                myAttributes.put("breakLabel", breakLabel);

                myAttributes.put("functionBeingDeclared", parentAttributes.get("functionBeingDeclared"));

                List<ParseTreeNode> children = node.getChildren();


                //<STAT> -> ID <STAT_ASSIGN_OR_FUNC> SEMI
                if(children.get(0).getNodeType() == Token.ID)
                {
                    generateCode(children.get(0));
                    Map<String, Object> idAttributes = attributes.get(children.get(0));
                    String idRegister = (String)idAttributes.get("register");

                    //only used if STAT_ASSIGN_OR_FUNC is a function call. ignored otherwise
                    myAttributes.put("functionBeingCalled", children.get(0).getLiteralToken());

                    //only used if STAT_ASSIGN_OR_FUNC is an assignment. ignored otherwise
                    myAttributes.put("register", idRegister);

                    generateCode(children.get(1));
                    generateCode(children.get(2));
                }
                //<STAT> -> WHILE <EXPR> DO <STAT_SEQ> ENDDO
                else if (children.get(0).getNodeType() == Token.WHILE)
                {
                    generateCode(children.get(0));

                    String loopLabel = "_WHILE_start" + nextWhileLabel;
                    String loopEndLabel = "_WHILE_end" + nextWhileLabel;
                    nextWhileLabel++;

                    myAttributes.put("breakLabel", loopEndLabel);

                    generateCode(children.get(0));

                    code.add("#");
                    code.add(loopLabel + ":");

                    code.add("\t#Evaluate while loop condition.");
                    generateCode(children.get(1));
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    String exprRegister = (String)exprAttributes.get("register");

                    code.add("\t#Test while loop condition.");
                    code.add(instruction("breq", exprRegister, "0", loopEndLabel));

                    generateCode(children.get(2));
                    generateCode(children.get(3));
                    generateCode(children.get(4));

                    code.add("\t#Return to top of loop.");
                    code.add(instruction("goto", loopLabel, null, null));

                    code.add("#");
                    code.add(loopEndLabel + ":");
                }
                //<STAT> FOR ID ASSIGN <EXPR> TO <EXPR> DO <STAT_SEQ> ENDDO SEMI
                else if (children.get(0).getNodeType() == Token.FOR)
                {
                    generateCode(children.get(0)); //FOR
                    generateCode(children.get(1)); //ID
                    generateCode(children.get(2)); //ASSIGN
                    generateCode(children.get(3)); //<EXPR>
                    generateCode(children.get(4)); //TO
                    generateCode(children.get(5)); //<EXPR>
                    generateCode(children.get(6)); //DO

                    String loopLabel = "_FOR_start" + nextForLabel;
                    String loopEndLabel = "_FOR_end" + nextForLabel;
                    nextForLabel++;

                    myAttributes.put("breakLabel", loopEndLabel);

                    Map<String, Object> idAttributes = attributes.get(children.get(1));
                    String idRegister = (String)idAttributes.get("register");

                    Map<String, Object> lowerBoundAttributes = attributes.get(children.get(3));
                    String lowerBoundRegister = (String)lowerBoundAttributes.get("register");

                    Map<String, Object> upperBoundAttributes = attributes.get(children.get(5));
                    String upperBoundRegister = (String)upperBoundAttributes.get("register");

                    code.add("\t#Initialize for loop counter");
                    code.add(instruction("assign", idRegister, lowerBoundRegister, null));

                    code.add("#");
                    code.add(loopLabel + ":");

                    code.add("\t#Test for loop condition.");
                    code.add(instruction("brgt", idRegister, upperBoundRegister, loopEndLabel));

                    generateCode(children.get(7)); //<STAT_SEQ>

                    generateCode(children.get(8));
                    generateCode(children.get(9));

                    code.add("\t#Increment for loop counter");
                    code.add(instruction("add", idRegister, "1", idRegister));
                    code.add("\t#Return to top of loop.");
                    code.add(instruction("goto", loopLabel, null, null));

                    code.add("#");
                    code.add(loopEndLabel + ":");
                }
                //<STAT> -> <IF_STAT> <IF_END> SEMI
                else if (children.get(0).getNodeType() == NonterminalSymbol.IF_STAT)
                {
                    generateCode(children.get(0));

                    Map<String, Object> ifStatAttributes = attributes.get(children.get(0));
                    String elseLabel = (String)ifStatAttributes.get("elseLabel");
                    String endIfLabel = (String)ifStatAttributes.get("endIfLabel");

                    myAttributes.put("elseLabel", elseLabel);
                    myAttributes.put("endIfLabel", endIfLabel);

                    generateCode(children.get(1));
                    generateCode(children.get(2));
                }
                //<STAT> -> RETURN <EXPR> SEMI
                else if(children.get(0).getNodeType() == Token.RETURN)
                {
                    for(ParseTreeNode child: children)
                        generateCode(child);

                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    String exprRegister = (String)exprAttributes.get("register");

                    code.add(instruction("return", exprRegister, null, null));
                }
                else if(children.get(0).getNodeType() == Token.BREAK)
                {
                    code.add(instruction("goto", breakLabel, null, null));
                }
            } break;

            case "STAT_SEQ":
            case "STAT_SEQ_CONT":
            {
                //<STAT_SEQ> -> <STAT> <STAT_SEQ_CONT>
                //<STAT_SEQ_CONT> -> <STAT_SEQ>
                //<STAT_SEQ_CONT> -> NULL

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());

                if(parentAttributes != null && parentAttributes.containsKey("breakLabel"))
                    myAttributes.put("breakLabel", parentAttributes.get("breakLabel"));

                for(ParseTreeNode child: node.getChildren())
                    generateCode(child);
            } break;

            case "IF_STAT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String breakLabel = (String)parentAttributes.get("breakLabel");
                myAttributes.put("breakLabel", breakLabel);

                String elseLabel = "_ELSE_start" + nextIfLabel;
                String endIfLabel = "_IF_end" + nextIfLabel;
                nextIfLabel++;

                myAttributes.put("elseLabel", elseLabel);
                myAttributes.put("endIfLabel", endIfLabel);

                //<IF_STAT> -> IF <EXPR> THEN <STAT_SEQ>


                code.add("\t#Evaluate if condition.");
                List<ParseTreeNode> children = node.getChildren();
                generateCode(children.get(0));
                generateCode(children.get(1));

                Map<String, Object> exprAttributes = attributes.get(children.get(1));
                String exprRegister = (String)exprAttributes.get("register");

                code.add("\t#Jump to else on false. If loop has no else clause,");
                code.add("\t#the else label will be at the same spot as the endif label");
                code.add(instruction("breq", exprRegister, "0", elseLabel));

                generateCode(children.get(2));
                generateCode(children.get(3));

                code.add(instruction("goto", endIfLabel, null, null));
            } break;

            case "IF_END":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String elseLabel = (String)parentAttributes.get("elseLabel");
                String endIfLabel = (String)parentAttributes.get("endIfLabel");

                code.add("#");
                code.add(elseLabel + ":");

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    generateCode(child);
                }

                code.add("#");
                code.add(endIfLabel + ":");
            } break;

            case "STAT_ASSIGN_OR_FUNC":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());

                List<ParseTreeNode> children = node.getChildren();

                //<STAT_ASSIGN_OR_FUNC> -> <LVALUE_TAIL> ASSIGN <STAT_ASSIGN_RHS>
                if(children.get(0).getNodeType() == NonterminalSymbol.LVALUE_TAIL)
                {
                    for(ParseTreeNode child: children)
                    {
                        generateCode(child);
                    }

                    String lhsRegister = (String)parentAttributes.get("register");

                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(0));
                    String indexRegister = (String)lValueTailAttributes.get("register");

                    Map<String, Object> statAssignRhsAttributes = attributes.get(children.get(2));
                    String statAssignRhsRegister = (String)statAssignRhsAttributes.get("register");

                    if(indexRegister == null)
                    {
                        code.add(instruction("assign", lhsRegister, statAssignRhsRegister, null));
                    }
                    else
                    {
                        code.add(instruction("array_store", lhsRegister, indexRegister, statAssignRhsRegister));
                    }
                }
                //<STAT_ASSIGN_OR_FUNC> -> <FUNC_CALL_END>
                else
                {
                    String funcName = (String)parentAttributes.get("functionBeingCalled");
                    myAttributes.put("functionBeingCalled", funcName);

                    generateCode(children.get(0));

                    FunctionSymbol function = (FunctionSymbol)globalSymbolTable.get(funcName);
                    int numParams = function.getParameterList().size();

                    code.add("\t#Function may or may not have return type. But since its return value");
                    code.add("\t#(if it has one) is unused, simply use \"call\" instead of \"callr\".");
                    String functionCall = "call, " + funcName;

                    for(int i = 0; i < numParams; i++)
                    {
                        functionCall += ", __" + funcName + "_arg" + i;
                    }

                    code.add("\t" + functionCall);
                }
            } break;

            case "EXPR_OR_FUNC_END":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();

                //<EXPR_OR_FUNC_END> -> <LVALUE_TAIL> <PRIME_TERM>
                if(children.get(0).getNodeType() == NonterminalSymbol.LVALUE_TAIL)
                {
                    generateCode(children.get(0));

                    Map<String, Object> parentAttributes = attributes.get(node.getParent());
                    String idRegister = (String) parentAttributes.get("register");

                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(0));
                    String indexRegister = (String) lValueTailAttributes.get("register");

                    if (indexRegister != null)
                    {
                        //look into the symbol table to find type info
                        //we can do this since idRegister is just the variable name
                        //for id's
                        String idStrippedSuffix = idRegister;
                        if(idRegister.endsWith("_int"))
                            idStrippedSuffix = idRegister.substring(0, idRegister.length() - 4);
                        else if (idRegister.endsWith("_float"))
                            idStrippedSuffix = idRegister.substring(0, idRegister.length() - 6);

                        VariableSymbol variable = (VariableSymbol)globalSymbolTable.get(idStrippedSuffix);
                        String tempRegister = getTempRegisterPrefix(variable.getType().baseType()) + nextTempRegister++;

                        code.add(instruction("array_load", tempRegister, idRegister, indexRegister));
                        myAttributes.put("register", tempRegister);
                    }
                    else
                    {
                        myAttributes.put("register", idRegister);
                    }


                    generateCode(children.get(1));
                    Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                    String primeTermRegister = (String)primeTermAttributes.get("register");

                    myAttributes.put("register", primeTermRegister);
                }
                //<EXPR_OR_FUNC_END> -> <FUNC_CALL_END>
                else if (children.get(0).getNodeType() == NonterminalSymbol.FUNC_CALL_END)
                {

                    Map<String, Object> parentAttributes = attributes.get(node.getParent());
                    String funcName = (String)parentAttributes.get("functionBeingCalled");
                    myAttributes.put("functionBeingCalled", funcName);

                    generateCode(children.get(0));

                    FunctionSymbol function = (FunctionSymbol)globalSymbolTable.get(funcName);

                    String resultRegister = getTempRegisterPrefix(function.getReturnType()) + nextTempRegister++;

                    int numParams = function.getParameterList().size();

                    //parameters were already stored in registers in <FUNC_CALL_END>
                    //register names are __funcName_arg0, __funcName_arg1, etc...

                    String funcCallString = "callr, " + resultRegister + ", " + funcName;
                    for(int i = 0; i < numParams; i++)
                    {
                        funcCallString += ", __" + funcName + "_arg" + i;
                    }

                    code.add("\t" + funcCallString);

                    myAttributes.put("register", resultRegister);
                }
            } break;

            case "LVALUE_TAIL":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    generateCode(child);
                }

                //<LVALUE_TAIL> -> NULL
                if(children.isEmpty())
                {
                    myAttributes.put("register", null);
                }
                //<LVALUE_TAIL> -> LBRACK <EXPR> RBRACK
                else
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    String exprRegister = (String)exprAttributes.get("register");

                    myAttributes.put("register", exprRegister);
                }
            } break;

            case "STAT_ASSIGN_RHS":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);


                List<ParseTreeNode> children = node.getChildren();

                //<STAT_ASSIGN_RHS> -> ID <EXPR_OR_FUNC_END>
                if(children.get(0).getNodeType() == Token.ID)
                {
                    generateCode(children.get(0));
                    Map<String, Object> idAttributes = attributes.get(children.get(0));
                    String idRegister = (String)idAttributes.get("register");

                    myAttributes.put("register", idRegister);

                    //only used if <EXPR_OR_FUNC_END> is a function. ignored otherwise
                    String funcName = children.get(0).getLiteralToken();
                    myAttributes.put("functionBeingCalled", funcName);

                    generateCode(children.get(1));
                    Map<String, Object> exprOrFuncEndAttributes = attributes.get(children.get(1));
                    String exprOrFuncEndRegister = (String)exprOrFuncEndAttributes.get("register");
                    myAttributes.put("register", exprOrFuncEndRegister);
                }
                //<STAT_ASSIGN_RHS> -> LPAREN <EXPR> RPAREN <PRIME_TERM>
                else if (children.get(0).getNodeType() == Token.LPAREN)
                {
                    generateCode(children.get(0));
                    generateCode(children.get(1));

                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    String exprRegister = (String)exprAttributes.get("register");

                    //prime term uses this register to calculate what will be in it's register
                    //our final register will be whatever is in the prime term register
                    myAttributes.put("register", exprRegister);

                    generateCode(children.get(2));
                    generateCode(children.get(3));

                    Map<String, Object> primeTermAttributes = attributes.get(children.get(3));
                    String primeTermRegister = (String) primeTermAttributes.get("register");

                    myAttributes.put("register", primeTermRegister);
                }
                //<STAT_ASSIGN_RHS> -> <CONST> <PRIME_TERM>
                else if (children.get(0).getNodeType() == NonterminalSymbol.CONST)
                {
                    generateCode(children.get(0));
                    Map<String, Object> constAttributes = attributes.get(children.get(0));
                    String constRegister = (String)constAttributes.get("register");

                    //prime term uses this register to calculate what will be in it's register
                    //our final register will be whatever is in the prime term register
                    myAttributes.put("register", constRegister);

                    generateCode(children.get(1));
                    Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                    String primeTermRegister = (String) primeTermAttributes.get("register");

                    myAttributes.put("register", primeTermRegister);
                }
            } break;


            case "FUNC_CALL_END":
            {
                //<FUNC_CALL_END> -> LPAREN <EXPR_LIST> RPAREN

                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String funcName = (String)parentAttributes.get("functionBeingCalled");

                myAttributes.put("functionBeingCalled", funcName);

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                    generateCode(child);

            } break;

            case "EXPR_LIST":
            case "EXPR_LIST_TAIL":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                int offset = 0;
                if(nodeTypeStr.equals("EXPR_LIST_TAIL"))
                    offset = 1;

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String funcName = (String)parentAttributes.get("functionBeingCalled");

                myAttributes.put("functionBeingCalled", funcName);

                List<ParseTreeNode> children = node.getChildren();

                //<EXPR_LIST> -> NULL
                //<EXPR_LIST_TAIL> -> NULL
                if(children.isEmpty())
                {
                    argumentNumber = 0;
                    return;
                }
                //<EXPR_LIST> -> <EXPR> <EXPR_LIST_TAIL>
                //<EXPR_LIST> -> COMMA <EXPR> <EXPR_LIST_TAIL>
                else
                {
                    generateCode(children.get(0 + offset));

                    //we could generate both, then emit code, but if we generate the first expr,
                    //emit code, then generate the tail, it emits the arguments in the same order they are in the source code
                    Map<String, Object> exprAttributes = attributes.get(children.get(0 + offset));
                    String exprRegister = (String)exprAttributes.get("register");
                    String registerName = "__" + funcName + "_arg" + argumentNumber;
                    argumentNumber++;

                    code.add(instruction("assign", registerName, exprRegister, null));

                    generateCode(children.get(1 + offset));
                }
            } break;

            case "PARAM":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String functionBeingDeclared = (String)parentAttributes.get("functionBeingDeclared");

                FunctionSymbol functionSymbol = (FunctionSymbol)globalSymbolTable.get(functionBeingDeclared);
                TypeSymbol myType = functionSymbol.getParameterList().get(argumentNumber);

                List<ParseTreeNode> children = node.getChildren();
                String paramName = children.get(0).getLiteralToken();

                String argumentRegister;
                if(myType.baseType() == TypeSymbol.INT)
                {
                    argumentRegister = "_" + functionBeingDeclared + "_" + paramName + "_int";
                }
                else
                {
                    argumentRegister = "_" + functionBeingDeclared + "_" + paramName + "_float";
                }

                String callSiteRegister = "__" + functionBeingDeclared + "_arg" + argumentNumber++;

                code.add(instruction("assign", argumentRegister, callSiteRegister, null));
            } break;


            case "PARAM_LIST":
            case "PARAM_LIST_TAIL":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String functionBeingDeclared = (String)parentAttributes.get("functionBeingDeclared");
                myAttributes.put("functionBeingDeclared", functionBeingDeclared);

                int offset = 0;
                if(nodeTypeStr.equals("PARAM_LIST_TAIL"))
                    offset = 1;

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    generateCode(child);
                }

                //<PARAM_LIST> -> NULL
                //<PARAM_LIST_TAIL> -> NULL
                if(children.isEmpty())
                {
                    argumentNumber = 0;
                }
            } break;


            case "EXPR":
            case "TERM1":
            case "TERM2":
            case "TERM3":
            case "TERM4":
            case "TERM5":
            {
                //<TERM#> -> <TERM # + 1> <Term # Prime>
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();

                generateCode(children.get(0));
                Map<String, Object> termAttributes = attributes.get(children.get(0));
                String termRegister = (String) termAttributes.get("register");
                myAttributes.put("register", termRegister);

                generateCode(children.get(1));
                Map<String, Object> primeTermAttributes = attributes.get(children.get(1));
                String primeTermRegister = (String) primeTermAttributes.get("register");

                if(primeTermRegister != null)
                    myAttributes.put("register", primeTermRegister);
            } break;

            case "PRIME_TERM":
            case "EXPR_PRIME":
            case "TERM1_PRIME":
            case "TERM2_PRIME":
            case "TERM3_PRIME":
            case "TERM4_PRIME":
            case "TERM5_PRIME":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                Map<String, Object> parentAttributes = attributes.get(node.getParent());
                String leftOpRegister = (String)parentAttributes.get("register");

                List<ParseTreeNode> children = node.getChildren();

                if(children.isEmpty())
                {
                    //this term doesn't actually do an operation (but the parent doesn't know that)
                    //so just give the parent register right back to the parent
                    myAttributes.put("register", leftOpRegister);
                }
                //<PRIME_TERM> -> <OP> <TERM> <TERM_PRIME>
                else
                {
                    generateCode(children.get(0));
                    generateCode(children.get(1));

                    Map<String, Object> termAttributes = attributes.get(children.get(1));
                    String rightOpRegister = (String)termAttributes.get("register");

                    Map<String, Object> mySemanticAttributes = semanticAttributes.get(node);
                    TypeSymbol myType = (TypeSymbol)mySemanticAttributes.get("type");

                    String resultRegister = getTempRegisterPrefix(myType) + nextTempRegister++;

                    GrammarSymbol childNodeType = children.get(0).getNodeType();

                    //and, or, add, sub, mul, div all have IR ops so they are straightforward. Equality_OP and INEQUALITY_OP
                    //are a little more involved...
                    if(childNodeType != NonterminalSymbol.EQUALITY_OP && childNodeType != NonterminalSymbol.INEQUALITY_OP)
                    {
                        String operation;
                        if (childNodeType == Token.OR)
                        {
                            operation = "or";
                        }
                        else if (childNodeType == Token.AND)
                        {
                            operation = "and";
                        }
                        else //ADD_SUB OR MUL_DIV
                        {
                            ParseTreeNode grandChild = children.get(0).getChildren().get(0);
                            Token grandChildNodeType = (Token) grandChild.getNodeType();

                            if (grandChildNodeType == Token.PLUS)
                            {
                                operation = "add";
                            }
                            else if (grandChildNodeType == Token.MINUS)
                            {
                                operation = "sub";
                            }
                            else if (grandChildNodeType == Token.MULT)
                            {
                                operation = "mult";
                            }
                            else //DIV
                            {
                                operation = "div";
                            }
                        }

                        code.add(instruction(operation, leftOpRegister, rightOpRegister, resultRegister));
                    }
                    else
                    {
                        ParseTreeNode grandChild = children.get(0).getChildren().get(0);
                        Token operation = (Token)grandChild.getNodeType();

                        //Equality or Inequality op
                        if(childNodeType == NonterminalSymbol.EQUALITY_OP)
                        {

                            String takeBranchLabel = "_EQ_true" + nextEqualityOpLabel;
                            String skipBranchLabel = "_EQ_false" + nextEqualityOpLabel;
                            nextEqualityOpLabel++;

                            String resultIfEqual = (operation == Token.EQ) ? "1" : "0";
                            String resultIfNotEqual = (operation == Token.EQ) ? "0" : "1";

                            code.add("\t#assume false, then change value if true");
                            code.add(instruction("assign", resultRegister, resultIfNotEqual, null));
                            code.add(instruction("breq", leftOpRegister, rightOpRegister, takeBranchLabel));
                            code.add(instruction("goto", skipBranchLabel, null, null));
                            code.add("#");
                            code.add(takeBranchLabel + ":");
                            code.add(instruction("assign", resultRegister, resultIfEqual, null));
                            code.add("#");
                            code.add(skipBranchLabel + ":");
                        }
                        else //INEQUALITY OP
                        {
                            String takeBranchLabel = "_INEQ_true" + nextInequalityOpLabel;
                            String skipBranchLabel = "_INEQ_false" + nextInequalityOpLabel;
                            nextInequalityOpLabel++;

                            boolean takeBranchIfEq = (operation == Token.LESSEREQ || operation == Token.GREATEREQ);
                            String opString = (operation == Token.LESSER || operation == Token.LESSEREQ) ? "brlt" : "brgt";

                            code.add("\t#assume false, then change value if true");
                            code.add(instruction("assign", resultRegister, "0", null));
                            code.add(instruction(opString, leftOpRegister, rightOpRegister, takeBranchLabel));

                            if(takeBranchIfEq)
                                code.add(instruction("breq", leftOpRegister, rightOpRegister, takeBranchLabel));

                            code.add(instruction("goto", skipBranchLabel, null, null));
                            code.add("#");
                            code.add(takeBranchLabel + ":");
                            code.add(instruction("assign", resultRegister, "1", null));
                            code.add("#");
                            code.add(skipBranchLabel + ":");
                        }
                    }

                    myAttributes.put("register", resultRegister);

                    generateCode(children.get(2));
                    Map<String, Object> primeTermAttributes = attributes.get(children.get(2));
                    String primeTermRegister = (String) primeTermAttributes.get("register");

                    if(primeTermRegister != null)
                        myAttributes.put("register", primeTermRegister);
                }
            } break;


            case "FACTOR":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();

                for(ParseTreeNode child: children)
                {
                    generateCode(child);
                }

                //<FACTOR> -> LPAREN <EXPR> RPAREN
                if(children.get(0).getNodeType() == Token.LPAREN)
                {
                    Map<String, Object> exprAttributes = attributes.get(children.get(1));
                    String exprRegister = (String)exprAttributes.get("register");

                    myAttributes.put("register", exprRegister);
                }
                //<FACTOR> -> ID <LVALUE_TAIL>
                else if(children.get(0).getNodeType() == Token.ID)
                {
                    Map<String, Object> idAttributes = attributes.get(children.get(0));
                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(1));

                    String idRegister = (String) idAttributes.get("register");
                    String indexRegister = (String) lValueTailAttributes.get("register");

                    //if indexing into array, we need to array_load into a temp register
                    //otherwise we can just use the variable's register directly
                    if (indexRegister != null)
                    {
                        String tempRegister;
                        Map<String, Object> idSemanticAttributes = semanticAttributes.get(children.get(0));
                        TypeSymbol idType = (TypeSymbol)idSemanticAttributes.get("type");

                        tempRegister = getTempRegisterPrefix(idType) + nextTempRegister++;

                        code.add(instruction("array_load", tempRegister, idRegister, indexRegister));

                        myAttributes.put("register", tempRegister);
                    }
                    else
                    {
                        myAttributes.put("register", idRegister);
                    }
                }
                //<FACTOR> -> <CONST>
                else if(children.get(0).getNodeType() == NonterminalSymbol.CONST)
                {
                    Map<String, Object> constAttributes = attributes.get(children.get(0));
                    String constRegister = (String)constAttributes.get("register");

                    myAttributes.put("register", constRegister);
                }

            } break;

            default:
            {
                Output.println("Internal compiler error. Could not find grammar symbol " + nodeTypeStr + " to generate IR code.");
                System.exit(-1);
            }
        }
    }

    private String instruction(String operation, String operand1, String operand2, String operand3)
    {
        String str0 = (operation == null) ? "" : operation;
        String str1 = (operand1 == null) ? "" : operand1;
        String str2 = (operand2 == null) ? "" : operand2;
        String str3 = (operand3 == null) ? "" : operand3;

        return "\t" + str0 + ", " + str1 + ", " + str2 + ", " + str3;
    }

    private String getTempRegisterPrefix(TypeSymbol type)
    {
        if(type == null)
            throw new IllegalArgumentException();

        TypeSymbol baseType = type.baseType();

        if(baseType == TypeSymbol.INT)
            return "_i";

        if(baseType == TypeSymbol.FLOAT)
            return "_f";

        throw new IllegalArgumentException();
    }
}
