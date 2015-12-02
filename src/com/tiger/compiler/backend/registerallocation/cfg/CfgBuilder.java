package com.tiger.compiler.backend.registerallocation.cfg;

import java.util.ArrayList;
import java.util.List;

public class CfgBuilder
{
    private String[] irCode;
    private List<CfgNode> cfg;
    private List<CfgNode> entryPoints; //each function has its own cfg, these are the entry points to each one

    public CfgBuilder(String[] irCode)
    {
        this.irCode = irCode;

        cfg = new ArrayList<>();
        entryPoints = new ArrayList<>();
    }

    public List<CfgNode> constructCfg()
    {
        //identify leaders and create basic blocks
        {
            int startLine = 0;
            int endLine;

            boolean declaringFunctions = true;
            boolean nextBlockIsEntryPoint = false;

            for (int i = 0; i < irCode.length; i++)
            {
                String line = irCode[i].trim();

                if (line.startsWith("#"))
                    continue;

                if (line.contains(":"))
                {
                    endLine = i - 1;
                    CfgNode node = new CfgNode(startLine, endLine);
                    cfg.add(node);

                    if(nextBlockIsEntryPoint)
                        entryPoints.add(node);

                    startLine = i;

                    if(declaringFunctions)
                    {
                        nextBlockIsEntryPoint = true;

                        if(line.equals("_program_start:"))
                            declaringFunctions = false;
                    }
                    else
                    {
                        nextBlockIsEntryPoint = false;
                    }
                }
                else if (line.startsWith("br") || line.startsWith("return") || line.startsWith("goto") ||
                        line.startsWith("call") || line.startsWith("callr"))
                {
                    endLine = i;
                    CfgNode node = new CfgNode(startLine, endLine);
                    cfg.add(node);

                    if(nextBlockIsEntryPoint)
                        entryPoints.add(node);

                    nextBlockIsEntryPoint = false;

                    startLine = i + 1;
                }
            }

            endLine = irCode.length - 1;
            if (endLine > startLine)
            {
                CfgNode node = new CfgNode(startLine, endLine);
                cfg.add(node);

                if(nextBlockIsEntryPoint)
                    entryPoints.add(node);
            }
        }

        //construct edges
        {
            for (CfgNode node : cfg)
            {
                String endLine = irCode[node.getEndLine()].trim();


                if(endLine.startsWith("br") || endLine.startsWith("goto"))
                {
                    String targetLabel = "";
                    if (endLine.startsWith("br"))
                    {
                        targetLabel = endLine.replaceAll(",", "").split(" ")[3];
                    }
                    else if (endLine.startsWith("goto"))
                    {
                        targetLabel = endLine.replaceAll(",", "").split(" ")[1];
                    }

                    for (CfgNode potentialTarget : cfg)
                    {
                        String startLine = irCode[potentialTarget.getStartLine()];
                        if (startLine.trim().equals(targetLabel + ":"))
                        {
                            node.addEdge(potentialTarget);
                            break;
                        }
                    }
                }

                if(!endLine.startsWith("goto") && !endLine.startsWith("return"))
                {
                    for(CfgNode potentialTarget : cfg)
                    {
                        if(potentialTarget.getStartLine() == node.getEndLine() + 1)
                        {
                            node.addEdge(potentialTarget);
                            break;
                        }
                    }
                }
            }
        }

        cfg.remove(0); //this is variable initialization, which doesn't use registers, and can only clutter

        return cfg;
    }

    public List<CfgNode> getEntryPoints()
    {
        return entryPoints;
    }
}
