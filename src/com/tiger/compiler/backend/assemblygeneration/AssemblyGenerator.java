package com.tiger.compiler.backend.assemblygeneration;

import com.tiger.compiler.Output;

import java.util.ArrayList;
import java.util.List;

public class AssemblyGenerator
{
    private String[] ir;
    private List<String> asm;

    private List<String> allVariables;

    public AssemblyGenerator(String[] ir)
    {
        this.ir = ir;
        asm = new ArrayList<>();

        allVariables = new ArrayList<>();
    }

    public String[] produceAssembly()
    {
        asm.add(".data");
        int varDeclEndLine = 0;

        asm.add("\n#User-created variables");
        for (int i = 0; i < ir.length; i++)
        {
            String line = ir[i];
            line = line.trim();

            if(line.isEmpty() || line.startsWith("#"))
                continue;

            String[] pieces = line.split(" ");

            if (pieces[0].equals("goto"))
            {
                varDeclEndLine = i;
                break; //var initialization complete
            }

            if(pieces[0].equals("assign"))
            {
                allVariables.add(pieces[1]);

                if(pieces.length == 3)
                {
                    //ignore temporary registers in the variable initialization segment.
                    //they are a side-product of the process through which the ir-generator
                    //emits code, but they are redundant and useless in this segment for the assembly
                    if(pieces[1].startsWith("_"))
                    {
                        continue;
                    }

                    if(pieces[1].endsWith("_int"))
                    {
                        asm.add(pieces[1] + ": .word " + pieces[2]);
                    }
                    else if(pieces[1].endsWith("_float"))
                    {
                        asm.add(pieces[1] + ": .float " + pieces[2]);
                    }
                    else
                    {
                        System.out.println("Internal compiler error.");
                        System.exit(-1);
                    }
                }
                else if (pieces.length == 4)
                {
                    if(pieces[1].endsWith("_int"))
                    {
                        asm.add(pieces[1] + ": .word " + pieces[3] + ":" + pieces[2]);
                    }
                    else if(pieces[1].endsWith("_float"))
                    {
                        asm.add(pieces[1] + ": .float " + pieces[3] + ":" + pieces[2]);
                    }
                    else
                    {
                        System.out.println("Internal compiler error.");
                        System.exit(-1);
                    }
                }
                else
                {
                    Output.println("Internal compiler error. Malformed 'assign' IR statement");
                    System.exit(-1);
                }
            }
            else
            {
                System.out.println("Internal compiler error. Non-assign IR statements before program body.");
                System.exit(-1);
            }
        }


        asm.add("\n#Compiler-created variables.");
        for (int i = varDeclEndLine + 1; i < ir.length; i++)
        {
            String line = ir[i].trim();
            if (line.isEmpty() || line.startsWith("#") || line.contains(":"))
                continue;

            String[] pieces = line.split(" ");

            if(pieces[0].equals("store_var"))
            {
                if(undiscoveredVariable(pieces[1]))
                    asm.add(pieces[1] + ": .word 0");
            }
            else if(pieces[0].equals("load_var"))
            {
                if(undiscoveredVariable(pieces[2]))
                    asm.add(pieces[2] + ": .word 0");
            }
            else if(pieces[0].equals("assign") && pieces.length == 4)
            {
                System.out.println("Internal compiler error. Assigning value to entire array should only occur in var_declaration segment.");
                System.exit(-1);
            }
        }

        if(undiscoveredVariable("__printi_arg0"))
            asm.add("__printi_arg0: .word 0");


        if(undiscoveredVariable("__printf_arg0"))
            asm.add("__printf_arg0: .word 0");

        asm.add("\n\n.text");

        //library functions
        asm.add("\nprinti:");
        asm.add("li $v0, 1");
        asm.add("syscall");
        asm.add("jr $ra");

        asm.add("\nprintf:");
        asm.add("li $v0, 2");
        asm.add("mtc1 $a0, $f12");
        asm.add("syscall");
        asm.add("jr $ra");

        for(int i = varDeclEndLine + 1; i < ir.length; i++)
        {
            String line = ir[i];
            line = line.trim();

            if(line.isEmpty() || line.startsWith("#"))
                continue;

            if(line.contains(":"))
            {
                asm.add("\n" + line);

                if(line.equals("_program_start:"))
                    asm.add("main:");

                continue;
            }

            String[] pieces = line.split(" ");

            switch(pieces[0])
            {
                case "assign":
                {
                    if(pieces.length == 4)
                    {
                        System.out.println("Internal compiler error. Array assign statement outside of initialization segment.");
                        System.exit(-1);
                    }

                    asm.add("move " + pieces[1] + ", " + pieces[2]);
                } break;

                case "add":
                case "sub":
                case "and":
                case "or":
                {
                    asm.add(pieces[0] + " " + pieces[3] + ", " + pieces[1] + ", " + pieces[2]);
                } break;

                case "mult":
                case "div":
                {
                    //mult and div put results in L and H registers.
                    //lets assume there won't be overflow and we just use L as the result...
                    asm.add(pieces[0] + " " + pieces[1] + ", " + pieces[2]);
                    asm.add("mflo " + pieces[3]);
                } break;

                case "goto":
                {
                    asm.add("j " + pieces[1]);
                } break;

                case "call":
                {
//                    int arg = 0;
//                    //put args in registers a0-a3
//                    //TODO: put additional args on stack
//                    for(int j = 2; j < pieces.length; j++)
//                    {
//                        asm.add("move $a" + arg + ", " + pieces[j]);
//
//                        if(arg == 3)
//                            break;
//
//                        arg++;
//                    }

                    asm.add("jal " + pieces[1]);
                } break;

                case "callr":
                {

                } break;

                case "return":
                {
                } break;

                case "breq":
                {
                    asm.add("beq " + pieces[1] + ", " + pieces[2] + ", " + pieces[3]);
                } break;

                case "brneq":
                {
                    asm.add("bne " + pieces[1] + ", " + pieces[2] + ", " + pieces[3]);
                } break;

                case "brlt":
                {
                    asm.add("blt " + pieces[1] + ", " + pieces[2] + ", " + pieces[3]);
                } break;

                case "brgt":
                {
                    asm.add("bgt " + pieces[1] + ", " + pieces[2] + ", " + pieces[3]);
                } break;

                case "brgeq":
                {
                    asm.add("bge " + pieces[1] + ", " + pieces[2] + ", " + pieces[3]);
                } break;

                case "brleq":
                {
                    asm.add("ble " + pieces[1] + ", " + pieces[2] + ", " + pieces[3]);
                } break;

                case "array_store":
                {
                    int offset = Integer.parseInt(pieces[2]);
                    offset *= 4;

                    asm.add("sw " + pieces[3] + ", " + pieces[1] + "(" + offset + ")");
                } break;

                case "array_load":
                {
                    int offset = Integer.parseInt(pieces[3]);
                    offset *= 4;

                    asm.add("lw " + pieces[1] + ", " + pieces[2] + "(" + offset + ")");
                } break;

                case "load_var":
                {
                    asm.add("lw " + pieces[1] + ", " + pieces[2]);
                } break;

                case "store_var":
                {
                    asm.add("sw " + pieces[2] + ", " + pieces[1]);
                } break;

                default:
                {
                    Output.println("\n\nERROR: Internal compiler error in AssemblyGenerator.");
                    System.exit(-1);
                }
            }
        }

        asm.add("\nli $v0, 10");
        asm.add("syscall");

        return asm.toArray(new String[asm.size()]);
    }

    public boolean undiscoveredVariable(String str)
    {
        if(str.matches("-?\\d+(\\.\\d+)?"))
            return false;
        else if(allVariables.contains(str))
            return false;

        allVariables.add(str);
        return true;
    }
}
