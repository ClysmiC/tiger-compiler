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
    private Map<ParseTreeNode, Map<String, Object>> semanticAttributes;
    private Map<ParseTreeNode, Map<String, Object>> attributes; //for IR generation, such as register name

    //stores the actual code lines that get generated
    private List<String> code;

    private int nextTemp;

    public TigerIrGenerator(ParseTreeNode parseTreeRoot, Map<ParseTreeNode, Map<String, Object>> parseTreeAttributes)
    {
        this.parseTreeRoot = parseTreeRoot;
        this.semanticAttributes = parseTreeAttributes;

        code = new LinkedList<String>();
        attributes = new HashMap<>();
        nextTemp = 0;
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
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                String idName = node.getLiteralToken();

                if(!myAttributes.containsKey("register"))
                    myAttributes.put("register", "_" + idName);
            } break;

            case "INTLIT":
            case "FLOATLIT":
            {
                Map<String, Object> myAttributes = new HashMap<>();
                attributes.put(node, myAttributes);

                String value = node.getLiteralToken();

                String register = "_" + nextTemp++;
                myAttributes.put("register", register);
                code.add(instruction("assign", register, value, null));
            } break;


            /**************************
             * NONTERMINALS
             **************************/
            case "TIGER_PROGRAM":
            case "DECLARATION_SEGMENT":
            case "TYPE_DECLARATION_LIST":
            case "VAR_DECLARATION_LIST":
            case "FUNC_DECLARATION_LIST":
            case "TYPE_DECLARATION":
            case "RET_TYPE":
            case "PARAM":
            case "STAT_SEQ":
            case "STAT_SEQ_CONT":
            case "INEQUALITY_OP":
            case "EQUALITY_OP":
            case "ADD_SUB_OP":
            case "MUL_DIV_OP":
            case "FUNC_DECLARATION":
            case "TYPE_SYMBOL":
            case "STAT":
            case "STAT_ASSIGN_OR_FUNC":
            case "STAT_ASSIGN_RHS":
            case "EXPR_OR_FUNC_END":
            case "FUNC_CALL_END":
            case "IF_STAT":
            case "IF_END":
            case "TYPE_ID":
            case "FACTOR":
            case "EXPR_LIST":
            case "EXPR_LIST_TAIL":
            case "PARAM_LIST":
            case "PARAM_LIST_TAIL":
            case "LVALUE_TAIL":
            case "PRIME_TERM":
            case "EXPR":
            case "TERM1":
            case "TERM2":
            case "TERM3":
            case "TERM4":
            case "TERM5":
            case "EXPR_PRIME":
            case "TERM1_PRIME":
            case "TERM2_PRIME":
            case "TERM3_PRIME":
            case "TERM4_PRIME":
            case "TERM5_PRIME":
            {
                List<ParseTreeNode> children = node.getChildren();

                for(ParseTreeNode child: children)
                    generateCode(child);
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

        return str0 + ", " + str1 + ", " + str2 + ", " + str3;
    }
}
