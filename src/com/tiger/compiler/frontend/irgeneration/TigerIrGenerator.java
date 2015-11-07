package com.tiger.compiler.frontend.irgeneration;

import com.tiger.compiler.Output;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;
import com.tiger.compiler.frontend.parser.NonterminalSymbol;
import com.tiger.compiler.frontend.parser.parsetree.ParseTreeNode;
import com.tiger.compiler.frontend.parser.symboltable.Symbol;
import com.tiger.compiler.frontend.parser.symboltable.TypeSymbol;

import java.util.*;

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
    private int nextBranchLabel;

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
        nextBranchLabel = 0;
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
            Output.println("Fatal error: Compiler failed to cast parse tree node type to either a token or nonterminal." +
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
                //IDs are stored in registers named _[id-name]
                //If the ID is a parameter to a function, we don't want to overwrite any global variables with the
                //same name, so we store it in register named __[function-name]_[id-name]
                //two underscores are required because there could be a global variable named [function-name]_[id-name]
                //and we don't want to risk overriding that
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);


                Map<String, Object> mySemanticAttributes = semanticAttributes.get(node);
                String functionName = (String)mySemanticAttributes.get("functionName");

                String idName = node.getLiteralToken();

                if(functionName == null) //not in a function
                {
                    myAttributes.put("register", "_" + idName);
                }
                else
                {
                    Map<String, Symbol> functionSymbolTable = (Map<String, Symbol>)mySemanticAttributes.get("functionSymbolTable");

                    Symbol param = functionSymbolTable.get(idName);

                    if(param == null) //it is a global variable, not a param
                    {
                        myAttributes.put("register", "_" + idName);
                    }
                    else //it is a parameter
                    {
                        myAttributes.put("register", "__" + functionName + "_" + idName);
                    }
                }
            } break;

            case "INTLIT":
            case "FLOATLIT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                String value = node.getLiteralToken();

                String register = "_t" + nextTempRegister++;
                myAttributes.put("register", register);
                code.add(instruction("assign", register, value, null));
            } break;


            /**************************
             * NONTERMINALS
             **************************/
            case "TYPE_DECLARATION_LIST":
            case "VAR_DECLARATION_LIST":
            case "FUNC_DECLARATION_LIST":
            case "TYPE_DECLARATION":
            case "RET_TYPE":
            case "PARAM":
            case "STAT_SEQ":
            case "STAT_SEQ_CONT":
            case "TYPE_SYMBOL":
            case "STAT":
            case "INEQUALITY_OP":
            case "EQUALITY_OP":
            case "ADD_SUB_OP":
            case "MUL_DIV_OP":
            case "EXPR_OR_FUNC_END":
            case "FUNC_CALL_END":
            case "IF_STAT":
            case "IF_END":
            case "TYPE_ID":
            case "EXPR_LIST":
            case "EXPR_LIST_TAIL":
            case "PARAM_LIST":
            case "PARAM_LIST_TAIL":
            {
                List<ParseTreeNode> children = node.getChildren();

                for(ParseTreeNode child: children)
                    generateCode(child);
            } break;

            case "TIGER_PROGRAM":
            {
                List<ParseTreeNode> children = node.getChildren();

                generateCode(children.get(0)); //LET
                generateCode(children.get(1)); //<DECLARATION_SEGMENT>
                generateCode(children.get(2)); //IN

                code.add("#Body of the actual program begins here.");
                code.add("___program_start:");

                generateCode(children.get(3)); //<STAT_SEQ>
                generateCode(children.get(4)); //END
            } break;

            case "DECLARATION_SEGMENT":
            {
                List<ParseTreeNode> children = node.getChildren();

                code.add("#All constants get stored in registers. These statements store the");
                code.add("#constants for custom type array sizes. They are likely never used.");
                generateCode(children.get(0));

                code.add("#");
                code.add("#Initialize variables, implicitly to 0, or explicitly to indicated value");
                generateCode(children.get(1));

                //after var declarations, jump to the main program code (don't want functions executing unless they're called)
                code.add(instruction("goto", "_program_start", null, null));
                code.add("#"); //prints blank line in debug mode

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

                if(myType.isArrayOfBaseType())
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
                    String constRegister = (String)constAttributes.get("register");
                    myAttributes.put("register", constRegister);
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
            } break;

            case "FUNC_DECLARATION":
            {
                List<ParseTreeNode> children = node.getChildren();

                generateCode(children.get(0)); //FUNCTION
                generateCode(children.get(1)); //ID

                String funcName = children.get(1).getLiteralToken();

                generateCode(children.get(2)); //LPAREN
                generateCode(children.get(3)); //<PARAM_LIST>
                generateCode(children.get(4)); //RPAREN
                generateCode(children.get(5)); //<RET_TYPE>
                generateCode(children.get(6)); //BEGIN

                code.add("funcName:");

                generateCode(children.get(7)); //<STAT_SEQ>
                generateCode(children.get(8)); //END
                generateCode(children.get(9)); //SEMI

            } break;

            case "STAT_ASSIGN_OR_FUNC":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                List<ParseTreeNode> children = node.getChildren();
                for(ParseTreeNode child: children)
                {
                    generateCode(child);
                }

                //<STAT_ASSIGN_OR_FUNC> -> <LVALUE_TAIL> ASSIGN <STAT_ASSIGN_RHS>
                if(children.get(0).getNodeType() == NonterminalSymbol.LVALUE_TAIL)
                {
                    //reach up through parent node to find the register we are storing
                    //the assignment in
                    ParseTreeNode parent_STAT = node.getParent(); //<STAT> -> ID <STAT_ASSIGN_OR_FUNC> SEMI
                    ParseTreeNode parent_child_ID = parent_STAT.getChildren().get(0);

                    Map<String, Object> parent_child_ID_attributes = attributes.get(parent_child_ID);
                    String lhsIdRegister = (String)parent_child_ID_attributes.get("register");

                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(0));
                    String indexRegister = (String)lValueTailAttributes.get("register");

                    Map<String, Object> statAssignRhsAttributes = attributes.get(children.get(2));
                    String statAssignRhsRegister = (String)statAssignRhsAttributes.get("register");

                    if(indexRegister == null)
                    {
                        code.add(instruction("assign", lhsIdRegister, statAssignRhsRegister, null));
                    }
                    else
                    {
                        code.add(instruction("array_store", lhsIdRegister, indexRegister, statAssignRhsRegister));
                    }
                }
                //<STAT_ASSIGN_OR_FUNC> -> <FUNC_CALL_END>
                else
                {

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
                String primeTermRegister = (String) termAttributes.get("register");

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

                    String resultRegister = "_t" + nextTempRegister++;

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
                        myAttributes.put("register", resultRegister);
                    }
                    else
                    {
                        ParseTreeNode grandChild = children.get(0).getChildren().get(0);
                        Token operation = (Token)grandChild.getNodeType();

                        //Equality or Inequality op
                        if(childNodeType == NonterminalSymbol.EQUALITY_OP)
                        {

                            String takeBranchLabel = "___EQ_OR_NEQ_true" + nextBranchLabel;
                            String skipBranchLabel = "___EQ_OR_NEQ_false" + nextBranchLabel;
                            nextBranchLabel++;

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
                            String takeBranchLabel = "___INEQ_true" + nextBranchLabel;
                            String skipBranchLabel = "___INEQ_false" + nextBranchLabel;
                            nextBranchLabel++;

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

                        generateCode(children.get(2));
                        Map<String, Object> primeTermAttributes = attributes.get(children.get(2));
                        String primeTermRegister = (String) primeTermAttributes.get("register");
                        myAttributes.put("register", primeTermRegister);
                    }

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
                    //we don't want to use the registers that hold the variables for RHS stuff,
                    //so we copy the ID value into a temp register (or the index of the id value
                    //we are grabbing)
                    Map<String, Object> idAttributes = attributes.get(children.get(0));
                    Map<String, Object> lValueTailAttributes = attributes.get(children.get(1));

                    String idRegister = (String) idAttributes.get("register");
                    String indexRegister = (String) lValueTailAttributes.get("register");

                    String tempRegister = "_t" + nextTempRegister++;

                    if (indexRegister != null)
                    {
                        code.add(instruction("array_load", tempRegister, idRegister, indexRegister));
                    }
                    else
                    {
                        code.add(instruction("assign", tempRegister, idRegister, null));
                    }

                    myAttributes.put("register", tempRegister);
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
}
