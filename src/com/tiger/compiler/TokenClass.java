package com.tiger.compiler;

public enum TokenClass
{
    START, COMMA, COLON, SEMI, LPAREN, RPAREN, LBRACK, RBRACK, LBRACE, RBRACE, PERIOD,
    PLUS, MINUS, MULT, DIV, EQ, NEQ, LESSER, GREATER, LESSEREQ, GREATEREQ, AND, OR, ASSIGN,
    ARRAY, BREAK, DO, ELSE, FOR, FUNC, IF, IN, LET, NIL, OF, THEN, TO, TYPE, VAR, WHILE, ENDIF, BEGIN, END, ENDDO,
    ID, INTLIT, FLOATLIT, WHITESPACE, ERROR, BLOCKCOMMENT, ILLEGAL;
    




    public static TokenClass classOf(DfaState dfaState, String str)
    {
        int id = dfaState.id();

        switch(id) {
            case 0:
                return START;

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
                switch(str.toUpperCase()) {
                    case "ARRAY":
                        return ARRAY;

                    case "BREAK":
                        return BREAK;

                    case "DO":
                        return DO;

                    case "ELSE":
                        return ELSE;

                    case "END":
                        return END;

                    case "FOR":
                        return FOR;

                    case "FUNC":
                        return FUNC;

                    case "IF":
                        return IF;

                    case "IN":
                        return IN;

                    case "LET":
                        return LET;

                    case "NIL":
                        return NIL;

                    case "OF":
                        return OF;

                    case "THEN":
                        return THEN;

                    case "TO":
                        return TO;

                    case "TYPE":
                        return TYPE;

                    case "VAR":
                        return VAR;

                    case "WHILE":
                        return WHILE;

                    case "ENDIF":
                        return ENDIF;

                    case "BEGIN":
                        return BEGIN;

                    case "ENDDO":
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

            case 24:
                return ILLEGAL;

            case 25:
                return ILLEGAL;
            /* BLOCKCOMMENT is not in description */
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