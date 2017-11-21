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
import java.io.Reader;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Lexer {

    private final Reader reader;
    private char ch;
    private int start_line = 0;
    private int start_pos = 0;
    private int end_line = 0;
    private int end_pos = 0;

    public Lexer(final Reader r) {
        this.reader = r;
    }

    public Token next() {
        throw new UnsupportedOperationException("not supported yet..");
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

        IdToken(Symbol sym, int start_line, int start_pos, int end_line, int end_pos, final String id) {
            super(sym, start_line, start_pos, end_line, end_pos);
            this.id = id;
        }
    }

    public static class IntToken extends Token {

        public final int val;

        IntToken(Symbol sym, int start_line, int start_pos, int end_line, int end_pos, final int val) {
            super(sym, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }
    }

    public static class RealToken extends Token {

        public final Rational val;

        RealToken(Symbol sym, int start_line, int start_pos, int end_line, int end_pos, final Rational val) {
            super(sym, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }
    }

    public static class StringToken extends Token {

        public final String val;

        StringToken(Symbol sym, int start_line, int start_pos, int end_line, int end_pos, final String val) {
            super(sym, start_line, start_pos, end_line, end_pos);
            this.val = val;
        }
    }
}
