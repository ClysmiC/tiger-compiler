package com.tiger.compiler.backend.registerallocation;

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
