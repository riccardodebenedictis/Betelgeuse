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
package it.cnr.istc.parser;

import it.cnr.istc.common.Rational;
import static it.cnr.istc.parser.Lexer.Symbol.ID;
import static it.cnr.istc.parser.Lexer.Symbol.INT;
import static it.cnr.istc.parser.Lexer.Symbol.REAL;
import static it.cnr.istc.parser.Lexer.Symbol.STRING;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Lexer {

    private final Reader reader;
    private int ch;
    private int start_line = 0;
    private int start_pos = 0;
    private int end_line = 0;
    private int end_pos = 0;

    public Lexer(final Reader r) {
        this.reader = r;
    }

    public Token next() throws IOException {
        ch = reader.read();
        throw new UnsupportedOperationException("not supported yet..");
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

    private RealToken mkRealToken(final String num, final String den) {
        RealToken tk = new RealToken(start_line, start_pos, end_line, end_pos, new Rational(Long.parseLong(num), Long.parseLong(den)));
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

    private void error(final String msg) {
        System.err.println(msg);
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

    public static class Token {

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
    }

    public static class IdToken extends Token {

        public final String id;

        IdToken(int start_line, int start_pos, int end_line, int end_pos, final String id) {
            super(ID, start_line, start_pos, end_line, end_pos);
            this.id = id;
        }
    }

    public static class IntToken extends Token {

        public final long val;

        IntToken(int start_line, int start_pos, int end_line, int end_pos, final long val) {
            super(INT, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }
    }

    public static class RealToken extends Token {

        public final Rational val;

        RealToken(int start_line, int start_pos, int end_line, int end_pos, final Rational val) {
            super(REAL, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }
    }

    public static class StringToken extends Token {

        public final String val;

        StringToken(int start_line, int start_pos, int end_line, int end_pos, final String val) {
            super(STRING, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }
    }
}
