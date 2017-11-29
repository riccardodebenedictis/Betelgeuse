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
package it.cnr.istc.smt.lra;

import static it.cnr.istc.smt.lra.Rational.ONE;
import static it.cnr.istc.smt.lra.Rational.ZERO;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class LRATheoryTest {

    @Test
    public void testLRATheory() {
        SatCore core = new SatCore();
        LRATheory lra = new LRATheory(core);

        int x = lra.newVar();
        int y = lra.newVar();
        int s1 = lra.newVar(new Lin(x, ONE.minus()).plus(new Lin(y)));
        int s2 = lra.newVar(new Lin(x, ONE).plus(new Lin(y)));

        // x <= -4
        boolean nc = core.newClause(new Lit(lra.newLEq(new Lin(x), new Lin(new Rational(-4))))) && core.check();
        Assert.assertTrue(nc);
        // x >= -8
        nc = core.newClause(new Lit(lra.newGEq(new Lin(x), new Lin(new Rational(-8))))) && core.check();
        Assert.assertTrue(nc);
        // s1 <= 1
        nc = core.newClause(new Lit(lra.newLEq(new Lin(s1), new Lin(ONE)))) && core.check();
        Assert.assertTrue(nc);

        // s2 >= -3
        boolean asm = core.assume(new Lit(lra.newGEq(new Lin(s2), new Lin(new Rational(-3)))));
        Assert.assertFalse(asm);
    }

    @Test
    public void testInequalities() {
        SatCore core = new SatCore();
        LRATheory lra = new LRATheory(core);

        int x = lra.newVar();
        int y = lra.newVar();

        // x >= y;
        boolean nc = core.newClause(new Lit(lra.newGEq(new Lin(x), new Lin(y)))) && core.check();
        Assert.assertTrue(nc);

        InfRational x_val = lra.value(x);
        Assert.assertTrue(x_val.eq(ZERO));

        InfRational y_val = lra.value(y);
        Assert.assertTrue(y_val.eq(ZERO));

        // y >= 1
        nc = core.newClause(new Lit(lra.newGEq(new Lin(y), new Lin(ONE)))) && core.check();
        Assert.assertTrue(nc);

        x_val = lra.value(x);
        Assert.assertTrue(x_val.eq(ONE));

        y_val = lra.value(y);
        Assert.assertTrue(y_val.eq(ONE));
    }

    @Test
    public void testStrictInequalities() {
        SatCore core = new SatCore();
        LRATheory lra = new LRATheory(core);

        int x = lra.newVar();
        int y = lra.newVar();

        // x > y;
        boolean nc = core.newClause(new Lit(lra.newGt(new Lin(x), new Lin(y)))) && core.check();
        Assert.assertTrue(nc);

        InfRational x_val = lra.value(x);
        Assert.assertTrue(x_val.eq(new InfRational(ZERO, ONE)));

        InfRational y_val = lra.value(y);
        Assert.assertTrue(y_val.eq(ZERO));

        // y >= 1
        nc = core.newClause(new Lit(lra.newGEq(new Lin(y), new Lin(ONE)))) && core.check();
        Assert.assertTrue(nc);

        x_val = lra.value(x);
        Assert.assertTrue(x_val.eq(new InfRational(ONE, ONE)));

        y_val = lra.value(y);
        Assert.assertTrue(y_val.eq(ONE));
    }
}
