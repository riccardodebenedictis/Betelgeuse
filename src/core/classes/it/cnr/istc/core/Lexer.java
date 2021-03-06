/*
 * Copyright (C) 2017 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.core;

import it.cnr.istc.smt.lra.Rational;
import static it.cnr.istc.core.Lexer.Symbol.*;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class Lexer {

    private final StringBuilder sb = new StringBuilder();
    private int pos = 0;
    private int ch;
    private int start_line = 0;
    private int start_pos = 0;
    private int end_line = 0;
    private int end_pos = 0;

    Lexer(final Reader r) throws IOException {
        char[] buff = new char[1024];
        int read;
        while ((read = r.read(buff)) != -1) {
            sb.append(buff, 0, read);
        }
        ch = nextChar();
    }

    Token next() throws ParsingException {
        switch (ch) {
            case '"': {
                // string literal..
                StringBuilder str = new StringBuilder();
                while (true) {
                    switch (ch = nextChar()) {
                        case '"':
                            return mkStringToken(str.toString());
                        case '\\':
                            // read escaped char..
                            str.append(nextChar());
                            break;
                        case '\r':
                        case '\n':
                            throw new ParsingException(start_line, start_pos, "newline in string literal..");
                        default:
                            str.append((char) ch);
                    }
                }
            }
            case '/':
                switch (ch = nextChar()) {
                    case '/': // in single-line comment
                        while (true) {
                            switch (ch = nextChar()) {
                                case '\r':
                                case '\n':
                                    return next();
                                case -1:
                                    return mkToken(EOF);
                            }
                        }
                    case '*': // in multi-line comment
                        while (true) {
                            switch (ch = nextChar()) {
                                case '*':
                                    if ((ch = nextChar()) == '/') {
                                        ch = nextChar();
                                        return next();
                                    }
                                    break;
                            }
                        }
                }
                return mkToken(SLASH);
            case '=':
                if ((ch = nextChar()) == '=') {
                    ch = nextChar();
                    return mkToken(EQEQ);
                }
                return mkToken(EQ);
            case '>':
                if ((ch = nextChar()) == '=') {
                    ch = nextChar();
                    return mkToken(GTEQ);
                }
                return mkToken(GT);
            case '<':
                if ((ch = nextChar()) == '=') {
                    ch = nextChar();
                    return mkToken(LTEQ);
                }
                return mkToken(LT);
            case '+':
                ch = nextChar();
                return mkToken(PLUS);
            case '-':
                ch = nextChar();
                return mkToken(MINUS);
            case '*':
                ch = nextChar();
                return mkToken(STAR);
            case '|':
                ch = nextChar();
                return mkToken(BAR);
            case '&':
                ch = nextChar();
                return mkToken(AMP);
            case '^':
                ch = nextChar();
                return mkToken(CARET);
            case '!':
                ch = nextChar();
                return mkToken(BANG);
            case '.':
                ch = nextChar();
                if ('0' <= ch && ch <= '9') {
                    // in a number literal..
                    StringBuilder dec = new StringBuilder();
                    dec.append((char) ch);
                    while (true) {
                        switch (ch = nextChar()) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                dec.append((char) ch);
                                break;
                            case '.':
                                throw new ParsingException(start_line, start_pos, "invalid numeric literal..");
                            default:
                                return mkRealToken("", dec.toString());
                        }
                    }
                }
                return mkToken(DOT);
            case ',':
                ch = nextChar();
                return mkToken(COMMA);
            case ';':
                ch = nextChar();
                return mkToken(SEMICOLON);
            case ':':
                ch = nextChar();
                return mkToken(COLON);
            case '(':
                ch = nextChar();
                return mkToken(LPAREN);
            case ')':
                ch = nextChar();
                return mkToken(RPAREN);
            case '[':
                ch = nextChar();
                return mkToken(LBRACKET);
            case ']':
                ch = nextChar();
                return mkToken(RBRACKET);
            case '{':
                ch = nextChar();
                return mkToken(LBRACE);
            case '}':
                ch = nextChar();
                return mkToken(RBRACE);
            case '0': // in a number literal..
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                StringBuilder intgr = new StringBuilder(); // the integer part..
                intgr.append((char) ch);
                while (true) {
                    switch (ch = nextChar()) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            intgr.append((char) ch);
                            break;
                        case '.': {
                            StringBuilder dcml = new StringBuilder(); // the decimal part..
                            while (true) {
                                switch (ch = nextChar()) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        dcml.append((char) ch);
                                        break;
                                    case '.':
                                        throw new ParsingException(start_line, start_pos, "invalid numeric literal..");
                                    default:
                                        return mkRealToken(intgr.toString(), dcml.toString());
                                }
                            }
                        }
                        default:
                            return mkIntToken(intgr.toString());
                    }
                }
            }
            case 'b': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'o') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'o') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'l') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(BOOL);
                }
            }
            case 'c': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'l') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'a') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 's') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 's') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(CLASS);
                }
            }
            case 'e': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'n') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'u') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'm') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(BOOL);
                }
            }
            case 'f': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'a') {
                    return finishId(str);
                }
                str.append((char) ch);
                switch (ch = nextChar()) {
                    case 'c':
                        str.append((char) ch);
                        if ((ch = nextChar()) != 't') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                            return finishId(str);
                        } else {
                            return mkToken(FACT);
                        }
                    case 'l':
                        str.append((char) ch);
                        if ((ch = nextChar()) != 's') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'e') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                            return finishId(str);
                        } else {
                            return mkToken(FALSE);
                        }
                    default:
                        return finishId(str);
                }
            }
            case 'g': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'o') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'a') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'l') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(GOAL);
                }
            }
            case 'i': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'n') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 't') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(INT);
                }
            }
            case 'n': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'e') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'w') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(NEW);
                }
            }
            case 'o': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'r') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(OR);
                }
            }
            case 'p': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'r') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'e') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'd') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'i') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'c') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'a') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 't') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'e') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(PREDICATE);
                }
            }
            case 'r': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'e') {
                    return finishId(str);
                }
                str.append((char) ch);
                switch (ch = nextChar()) {
                    case 'a':
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'l') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                            return finishId(str);
                        } else {
                            return mkToken(REAL);
                        }
                    case 't':
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'u') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'r') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'n') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                            return finishId(str);
                        } else {
                            return mkToken(RETURN);
                        }
                    default:
                        return finishId(str);
                }
            }
            case 's': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 't') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'r') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'i') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'n') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'g') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(STRING);
                }
            }
            case 't': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                switch (ch = nextChar()) {
                    case 'r':
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'u') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'e') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                            return finishId(str);
                        } else {
                            return mkToken(TRUE);
                        }
                    case 'y':
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'p') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'e') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'd') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'e') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != 'f') {
                            return finishId(str);
                        }
                        str.append((char) ch);
                        if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                            return finishId(str);
                        } else {
                            return mkToken(TYPEDEF);
                        }
                    default:
                        return finishId(str);
                }
            }
            case 'v': {
                StringBuilder str = new StringBuilder();
                str.append((char) ch);
                if ((ch = nextChar()) != 'o') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'i') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != 'd') {
                    return finishId(str);
                }
                str.append((char) ch);
                if ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
                    return finishId(str);
                } else {
                    return mkToken(OR);
                }
            }
            case 'a':
            case 'd':
            case 'h':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'q':
            case 'u':
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
            case '_':
                return finishId(new StringBuilder());
            case '\t':
            case ' ':
            case '\r':
            case '\n':
                while (true) {
                    switch (ch = nextChar()) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            break;
                        case -1:
                            return mkToken(EOF);
                        default:
                            return next();
                    }
                }
            case -1:
                return mkToken(EOF);
            default:
                throw new ParsingException(start_line, start_pos, "invalid token..");
        }
    }

    private int nextChar() {
        if (pos == sb.length()) {
            return -1;
        } else {
            switch (sb.charAt(pos)) {
                case ' ':
                    start_pos++;
                    end_pos++;
                    break;
                case '\t':
                    start_pos += 4 - (start_pos % 4);
                    end_pos += 4 - (end_pos % 4);
                    break;
                case '\r':
                    if (pos + 1 != sb.length() && sb.charAt(pos + 1) == '\n') {
                        pos++;
                        end_line++;
                        end_pos = 0;
                        break;
                    }
                case '\n':
                    end_line++;
                    end_pos = 0;
                    break;
                default:
                    end_pos++;
                    break;
            }
            return sb.charAt(pos++);
        }
    }

    private Token mkToken(final Symbol sym) {
        Token tk = new Token(sym, start_line, start_pos, end_line, end_pos);
        start_line = end_line;
        start_pos = end_pos;
        return tk;
    }

    private IdToken mkIdToken(final String id) {
        IdToken tk = new IdToken(start_line, start_pos, end_line, end_pos, id);
        start_line = end_line;
        start_pos = end_pos;
        return tk;
    }

    private IntToken mkIntToken(final String val) {
        IntToken tk = new IntToken(start_line, start_pos, end_line, end_pos, Long.parseLong(val));
        start_line = end_line;
        start_pos = end_pos;
        return tk;
    }

    private RealToken mkRealToken(final String intgr, final String dec) {
        RealToken tk = new RealToken(start_line, start_pos, end_line, end_pos, new Rational(Long.parseLong(intgr + dec), (long) Math.pow(10, dec.length())));
        start_line = end_line;
        start_pos = end_pos;
        return tk;
    }

    private StringToken mkStringToken(final String str) {
        StringToken tk = new StringToken(start_line, start_pos, end_line, end_pos, str);
        start_line = end_line;
        start_pos = end_pos;
        return tk;
    }

    private Token finishId(final StringBuilder str) throws ParsingException {
        if (!is_id_part((char) ch)) {
            return mkIdToken(str.toString());
        }
        str.append((char) ch);
        while ((ch = nextChar()) != -1 && is_id_part((char) ch)) {
            str.append((char) ch);
        }
        return mkIdToken(str.toString());
    }

    private static boolean is_id_part(final char ch) {
        return ch == '_' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
    }

    public enum Symbol {
        BOOL, // 'bool'
        INT, // 'int'
        REAL, // 'real'
        STRING, // 'string'
        TYPEDEF, // 'typedef'
        ENUM, // 'enum'
        CLASS, // 'class'
        GOAL, // 'goal'
        FACT, // 'fact'
        PREDICATE, // 'predicate'
        NEW, // 'new'
        OR, // 'or'
        THIS, // 'this'
        VOID, // 'void'
        TRUE, // 'true'
        FALSE, // 'false'
        RETURN, // 'return'
        DOT, // '.'
        COMMA, // ','
        COLON, // ':'
        SEMICOLON, // ';'
        LPAREN, // '('
        RPAREN, // ')'
        LBRACKET, // '['
        RBRACKET, // ']'
        LBRACE, // '{'
        RBRACE, // '}'
        PLUS, // '+'
        MINUS, // '-'
        STAR, // '*'
        SLASH, // '/'
        AMP, // '&'
        BAR, // '|'
        EQ, // '='
        GT, // '>'
        LT, // '<'
        BANG, // '!'
        EQEQ, // '=='
        LTEQ, // '<='
        GTEQ, // '>='
        BANGEQ, // '!='
        IMPLICATION, // '->'
        CARET, // '^'
        ID, // ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
        IntLiteral, // [0-9]+
        RealLiteral, // [0-9]+ '.' [0-9]+)? | '.' [0-9]+
        StringLiteral, // '" . . ."'
        EOF
    }

    static class Token {

        public final Symbol sym;
        public final int start_line;
        public final int start_pos;
        public final int end_line;
        public final int end_pos;

        Token(Symbol sym, int start_line, int start_pos, int end_line, int end_pos) {
            this.sym = sym;
            this.start_line = start_line;
            this.start_pos = start_pos;
            this.end_line = end_line;
            this.end_pos = end_pos;
        }

        @Override
        public String toString() {
            return sym + " [" + start_line + ", " + start_pos + "]";
        }
    }

    static class IdToken extends Token {

        public final String id;

        IdToken(int start_line, int start_pos, int end_line, int end_pos, final String id) {
            super(ID, start_line, start_pos, end_line, end_pos);
            this.id = id;
        }

        @Override
        public String toString() {
            return super.toString() + " " + id;
        }
    }

    static class IntToken extends Token {

        public final long val;

        IntToken(int start_line, int start_pos, int end_line, int end_pos, final long val) {
            super(IntLiteral, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }

        @Override
        public String toString() {
            return super.toString() + " " + val;
        }
    }

    static class RealToken extends Token {

        public final Rational val;

        RealToken(int start_line, int start_pos, int end_line, int end_pos, final Rational val) {
            super(RealLiteral, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }

        @Override
        public String toString() {
            return super.toString() + " " + val;
        }
    }

    static class StringToken extends Token {

        public final String val;

        StringToken(int start_line, int start_pos, int end_line, int end_pos, final String val) {
            super(StringLiteral, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }

        @Override
        public String toString() {
            return super.toString() + " " + val;
        }
    }
}
