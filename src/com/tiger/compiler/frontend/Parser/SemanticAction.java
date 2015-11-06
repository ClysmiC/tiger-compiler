package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.frontend.GrammarSymbol;

/**
 * Created by Andrew on 11/1/2015.
 */
public enum SemanticAction implements GrammarSymbol
{
    PUT_ID_STACK,
    PUT_TYPE_STACK,
    PUT_INT_STACK,
    PUT_TRUE_STACK,
    PUT_FALSE_STACK,

    PUT_TYPE_TABLE,
    PUT_VARS_TABLE,
    PUT_FUNC_TABLE,

    //makes sure you don't use break when not in a loop
    LOOP_ENTER,
    LOOP_EXIT,
    LOOP_BREAK
}
