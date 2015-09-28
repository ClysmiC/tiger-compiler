public enum CharClass
{
    COMMA, SEMICOLON, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET,
    RBRACKET, COLON, EQ, PERIOD, PLUS, MINUS, MULT, DIV, LESS,
    GREATER, AND, LETTER, UNDERSCORE, NUMNOTZERO, ZERO, OR, WHITESPACE, OTHER;

    public static CharClass classOf(char c)
    {
        switch(c)
        {
            case ',':
                return COMMA;
            
            case ';':
                return SEMICOLON;
            
            case '(':
                return LPAREN;
            
            case ')':
                return RPAREN;
            
            case '{':
                return LBRACE;
            
            case '}':
                return RBRACE;
            
            case '[':
                return LBRACKET;
            
            case ']':
                return RBRACKET;
            
            case ':':
                return COLON;
            
            case '=':
                return EQ;
            
            case '.':
                return PERIOD;
            
            case '+':
                return COMMA;
            
            case '-':
                return COMMA;
            
            case '*':
                return COMMA;
            
            case '/':
                return COMMA;
            
            case '<':
                return COMMA;
            
            case '>':
                return COMMA;
            
            case '&':
                return COMMA;
            
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                return LETTER;

            case '_':
                return UNDERSCORE;

            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return NUMNOTZERO;

            case '0':
                return ZERO;

            case '|':
                return OR;

            case ' ':
            case '\n':
            case '\t':
                return WHITESPACE;

            default:
                return OTHER;
        }
    }
}