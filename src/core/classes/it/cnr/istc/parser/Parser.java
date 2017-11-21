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

import it.cnr.istc.parser.Lexer.Token;
import it.cnr.istc.parser.declarations.CompilationUnit;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Parser {

    private Token tk; // the current lookahead token..
    private final List<Token> tks = new ArrayList<>(); // all the tokens parsed so far..
    private int pos = 0; // the current position within 'tks'..

    public CompilationUnit parse(final Reader r) {
        Lexer lexer = new Lexer(r);
        throw new UnsupportedOperationException("not supported yet..");
    }

    private Token next() {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private boolean match(final Lexer.Symbol s) {
        throw new UnsupportedOperationException("not supported yet..");
    }

    private void backtrack(final int p) {
        throw new UnsupportedOperationException("not supported yet..");
    }
}
