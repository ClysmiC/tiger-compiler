package com.tiger.compiler.frontend;

import com.tiger.compiler.frontend.scanner.DfaState;

public enum Token
{
    COMMA, COLON, SEMI, LPAREN, RPAREN, LBRACK, RBRACK, LBRACE, RBRACE, PERIOD,
    PLUS, MINUS, MULT, DIV, EQ, NEQ, LESSER, GREATER, LESSEREQ, GREATEREQ, AND, OR, ASSIGN,
    ARRAY, BREAK, DO, ELSE, FOR, FUNCTION, RETURN, IF, IN, LET, OF, THEN, TO, TYPE, VAR, INT, FLOAT, WHILE, ENDIF, BEGIN, END, ENDDO,
    ID, INTLIT, FLOATLIT, WHITESPACE, ERROR, BLOCKCOMMENT, NULL, EOF;


    public static Token classOf(DfaState dfaState, String str)
    {
        int id = dfaState.id();

        switch(id) {

            case 1:
                return COMMA;

            case 2:
                return SEMI;

            case 3:
                return LPAREN;

            case 4:
                return RPAREN;

            case 5:
                return LBRACE;

            case 6:
                return RBRACE;

            case 7:
                return LBRACK;

            case 8:
                return RBRACK;

            case 9:
                return COLON;

            case 10:
                return PERIOD;

            case 11:
                return PLUS;

            case 12:
                return MINUS;

            case 13:
                return MULT;

            case 14:
                return DIV;

            case 15:
                return EQ;

            case 16:
                return LESSER;

            case 17:
                return GREATER;

            case 18:
                return AND;

            case 19:
                /* this is ID */
                switch(str) {
                    case "array":
                        return ARRAY;

                    case "break":
                        return BREAK;

                    case "do":
                        return DO;

                    case "else":
                        return ELSE;

                    case "end":
                        return END;

                    case "for":
                        return FOR;

                    case "function":
                        return FUNCTION;

                    case "return":
                        return RETURN;

                    case "if":
                        return IF;

                    case "in":
                        return IN;

                    case "let":
                        return LET;

                    case "of":
                        return OF;

                    case "then":
                        return THEN;

                    case "to":
                        return TO;

                    case "type":
                        return TYPE;

                    case "var":
                        return VAR;

                    case "int":
                        return INT;

                    case "float":
                        return FLOAT;

                    case "while":
                        return WHILE;

                    case "endif":
                        return ENDIF;

                    case "begin":
                        return BEGIN;

                    case "enddo":
                        return ENDDO;

                    default:
                        return ID;
                }

            case 20:
                return OR;

            case 21:
                return INTLIT;

            case 22:
                return INTLIT;

            case 23:
                return ASSIGN;

            case 26:
                return BLOCKCOMMENT;

            case 27:
                return NEQ;

            case 28:
                return LESSEREQ;

            case 29:
                return GREATEREQ;

            case 30:
                return FLOATLIT;

            case 31:
                return FLOATLIT;

            case 32:
                return WHITESPACE;

            default:
                return ERROR;
        }
    }
}