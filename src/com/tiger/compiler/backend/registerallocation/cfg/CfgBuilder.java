package com.tiger.compiler.backend.registerallocation.cfg;

import java.util.ArrayList;
import java.util.List;

public class CfgBuilder
{
    private String[] irCode;
    private List<CfgNode> cfg;

    public CfgBuilder(String[] irCode)
    {
        this.irCode = irCode;

        cfg = new ArrayList<>();
    }

    public List<CfgNode> constructCfg()
    {
        //identify leaders and create basic blocks
        {
            int startLine = 0;
            int endLine;

            for (int i = 0; i < irCode.length; i++)
            {
                String line = irCode[i].trim();

                if (line.startsWith("#"))
                    continue;

                if (line.contains(":"))
                {
                    endLine = i - 1;
                    cfg.add(new CfgNode(startLine, endLine));

                    startLine = i;
                }
                else if (line.startsWith("br") || line.startsWith("return") || line.startsWith("goto") ||
                        line.startsWith("call") || line.startsWith("callr"))
                {
                    endLine = i;
                    cfg.add(new CfgNode(startLine, endLine));

                    startLine = i + 1;
                }
            }

            endLine = irCode.length - 1;
            if (endLine > startLine)
                cfg.add(new CfgNode(startLine, endLine));
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

                if(endLine.startsWith("call") || endLine.startsWith("callr"))
                {
                    String targetLabel = "";
                    if (endLine.startsWith("callr"))
                    {
                        targetLabel = endLine.replaceAll(",", "").split(" ")[2];
                    }
                    else if (endLine.startsWith("call"))
                    {
                        targetLabel = endLine.replaceAll(",", "").split(" ")[1];
                    }

                    //edge from me to function i'm calling
                    for (CfgNode potentialTarget : cfg)
                    {
                        String startLine = irCode[potentialTarget.getStartLine()];
                        if (startLine.trim().equals(targetLabel + ":"))
                        {
                            node.addEdge(potentialTarget);
                            break;

                            //add edge from function 
                        }
                    }
                }
                else if(!endLine.startsWith("goto") && !endLine.startsWith("return"))
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

        return cfg;
    }
}
