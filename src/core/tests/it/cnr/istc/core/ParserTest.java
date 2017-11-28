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

import java.io.StringReader;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class ParserTest {

    @Test
    public void testParser() throws Exception {
        Parser p0 = new Parser(new Lexer(new StringReader("real a;\n1 <= a;")));
        CompilationUnit cu0 = p0.compilation_unit();

        Parser p1 = new Parser(new Lexer(new StringReader("real a = 5 +2;\nfalse;")));
        CompilationUnit cu1 = p1.compilation_unit();

        Parser p2 = new Parser(new Lexer(new StringReader("goal g0 = new At(l:5+3);")));
        CompilationUnit cu2 = p2.compilation_unit();
    }
}
