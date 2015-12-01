package com.tiger.compiler.backend.registerallocation;

import com.tiger.compiler.backend.registerallocation.cfg.CfgBuilder;

public class BasicBlockAllocator extends RegisterAllocator
{
    public BasicBlockAllocator(String[] irCode)
    {
        super(irCode);
    }

    @Override
    public String[] insertAllocationStatements()
    {
        CfgBuilder cfgBuilder = new CfgBuilder(oldIr);
        return newIr.toArray(new String[newIr.size()]);
    }
}
