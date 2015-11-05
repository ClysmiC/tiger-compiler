package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.frontend.GrammarSymbol;

public enum NonterminalSymbol implements GrammarSymbol
{
    ERROR,
    TIGER_PROGRAM,
    DECLARATION_SEGMENT,
    TYPE_DECLARATION_LIST,
    VAR_DECLARATION_LIST,
    FUNC_DECLARATION_LIST,
    TYPE_DECLARATION,
    TYPE,
    TYPE_ID,
    VAR_DECLARATION,
    ID_LIST,
    ID_LIST_TAIL,
    OPTIONAL_INIT,
    FUNC_DECLARATION,
    PARAM_LIST,
    PARAM_LIST_TAIL,
    RET_TYPE,
    PARAM,
    STAT_SEQ,
    STAT_SEQ_CONT,
    STAT,
    STAT_ASSIGN_OR_FUNC,
    STAT_ASSIGN_RHS,
    EXPR_OR_FUNC_END,
    FUNC_CALL_END,
    IF_STAT,
    IF_END,
    EXPR,
    CONST,
    TERM1,
    TERM2,
    TERM3,
    TERM4,
    TERM5,
    FACTOR,
    INEQUALITY_OP,
    EQUALITY_OP,
    ADD_SUB_OP,
    MUL_DIV_OP,
    EXPR_LIST,
    EXPR_LIST_TAIL,
    LVALUE_TAIL,
    EXPR_PRIME,
    TERM1_PRIME,
    TERM2_PRIME,
    TERM3_PRIME,
    TERM4_PRIME,
    TERM5_PRIME,
    PRIME_TERM
}
