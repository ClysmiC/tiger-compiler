package com.tiger.compiler.frontend.parser.parsetree;

import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;
import com.tiger.compiler.frontend.parser.NonterminalSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 11/4/2015.
 */
public class ParseTreeNode
{
    private ParseTreeNode parent;
    private GrammarSymbol nodeType;
    private List<ParseTreeNode> children;

    private int nextChildToVisit = 0;

    public ParseTreeNode(ParseTreeNode parent, GrammarSymbol nodeType)
    {
        this(parent, nodeType, null);
    }

    public ParseTreeNode(ParseTreeNode parent, GrammarSymbol nodeType, List<ParseTreeNode> children)
    {
        this.parent = parent;
        this.nodeType = nodeType;
        this.children = new ArrayList<>();

        if(children != null)
        {
            //don't add semantic actions to the parse tree
            for(ParseTreeNode child: children)
            {
                if(child.nodeType instanceof NonterminalSymbol || child.nodeType instanceof Token)
                    this.children.add(child);
            }
        }
    }

    public void setChildren(List<ParseTreeNode> children)
    {
        this.children.clear();

        if(children != null)
        {
            //don't add semantic actions to the parse tree
            for(ParseTreeNode child: children)
            {
                if(child.nodeType instanceof NonterminalSymbol || child.nodeType instanceof Token)
                    this.children.add(child);
            }
        }
    }

    public List<ParseTreeNode> getChildren()
    {
        return children;
    }

    public ParseTreeNode getParent()
    {
        return parent;
    }

    /**
     * Method that essentially acts as an iterator, returning the next node needed for a pre-order traversal.
     * This takes the logic out of the parser as it travels along the nodes adding new children.
     *
     */
    public ParseTreeNode nextNodePreOrder()
    {
        if(nextChildToVisit < children.size())
        {
            ParseTreeNode returnVal = children.get(nextChildToVisit);
            nextChildToVisit++;
            return returnVal;
        }

        if(parent == null)
            return null;

        return parent.nextNodePreOrder();
    }

    public String nodeToString(int indentation)
    {
        String str = "";

        for(int i = 0; i < indentation; i++)
            str += " ";

        str += nodeType.toString();
        str += "\n";

        for(ParseTreeNode child: children)
        {
            str += child.nodeToString(indentation + 1);
        }

        return str;
    }
}
