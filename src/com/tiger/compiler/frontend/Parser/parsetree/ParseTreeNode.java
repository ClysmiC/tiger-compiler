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
    private List<GrammarSymbol> children;

    public ParseTreeNode(ParseTreeNode parent, List<GrammarSymbol> children)
    {
        this.parent = parent;
        this.children = new ArrayList<>();

        if(children != null)
        {
            //don't add semantic actions to the parse tree
            for(GrammarSymbol g: children)
            {
                if(g instanceof NonterminalSymbol || g instanceof Token)
                    this.children.add(g);
            }
        }
    }
}
