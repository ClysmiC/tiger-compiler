package com.tiger.compiler.backend.registerallocation.cfg;

import java.util.ArrayList;
import java.util.List;

public class CfgNode
{
    private int startLine; //inclusive
    private int endLine; //inclusive
    private List<CfgNode> edges;

    public CfgNode(int startLine, int endLine)
    {
        this.startLine = startLine;
        this.endLine = endLine;

        edges = new ArrayList<>();
    }

    public int getStartLine()
    {
        return startLine;
    }

    public int getEndLine()
    {
        return endLine;
    }

    public void addEdge(CfgNode target)
    {
        edges.add(target);
    }
}
