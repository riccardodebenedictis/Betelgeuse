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

import java.io.StringReader;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class LexerTest {

    @Test
    public void testNext() throws Exception {
        Lexer instance = new Lexer(new StringReader("real a = 5 +2;\nfalse;"));
        Lexer.Token t0 = instance.next();
        Assert.assertEquals(t0.sym, Lexer.Symbol.REAL);
        Lexer.Token t1 = instance.next();
        Assert.assertEquals(t1.sym, Lexer.Symbol.ID);
        Lexer.Token t2 = instance.next();
        Assert.assertEquals(t2.sym, Lexer.Symbol.EQ);
        Lexer.Token t3 = instance.next();
        Assert.assertEquals(t3.sym, Lexer.Symbol.IntLiteral);
        Lexer.Token t4 = instance.next();
        Assert.assertEquals(t4.sym, Lexer.Symbol.PLUS);
        Lexer.Token t5 = instance.next();
        Assert.assertEquals(t5.sym, Lexer.Symbol.IntLiteral);
        Lexer.Token t6 = instance.next();
        Assert.assertEquals(t6.sym, Lexer.Symbol.SEMICOLON);
        Lexer.Token t7 = instance.next();
        Assert.assertEquals(t7.sym, Lexer.Symbol.FALSE);
        Lexer.Token t8 = instance.next();
        Assert.assertEquals(t8.sym, Lexer.Symbol.SEMICOLON);
        Lexer.Token t9 = instance.next();
        Assert.assertEquals(t9.sym, Lexer.Symbol.EOF);
    }
}
