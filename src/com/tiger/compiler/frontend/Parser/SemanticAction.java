package com.tiger.compiler.frontend.parser;

/**
 * Created by Andrew on 11/1/2015.
 */
public enum SemanticAction
{
    PUT_TYPE,
    CHECK_VALID_TYPE,

    APPEND_VAR_TO_LIST,
    ADD_TYPE_INFO_TO_VAR_LIST,
    OPT_INIT_VAR_LIST,
    INIT_CHECK_TYPE_COMPATIBILITY,
    PUT_VAR_LIST,

    PUT_FUNC,
    INIT_FUNC_SYMBOL_TABLE,
    CLEAR_FUNC_SYMBOL_TABLE,
    PUT_PARAM
}
