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

import it.cnr.istc.common.Lin;
import it.cnr.istc.common.Rational;
import static it.cnr.istc.common.Rational.ONE;
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

        int x0 = lra.newVar();
        int x1 = lra.newVar();
        int x2 = lra.newVar();
        int x3 = lra.newVar();

        // x0 == -x2+x3
        Lin l_x0 = new Lin(x0);
        Lin l__x2x3 = new Lin(x2, ONE.minus());
        l__x2x3.vars.put(x3, ONE);
        boolean nc = core.newClause(new Lit(core.newConj(new Lit(lra.newLEq(l_x0, l__x2x3)), new Lit(lra.newGEq(l_x0, l__x2x3))))) && core.check();
        Assert.assertTrue(nc);

        //x1 == x2+x3
        Lin l_x1 = new Lin(x1);
        Lin l_x2x3 = new Lin(x2, ONE);
        l_x2x3.vars.put(x3, ONE);
        nc = core.newClause(new Lit(core.newConj(new Lit(lra.newLEq(l_x1, l_x2x3)), new Lit(lra.newGEq(l_x1, l_x2x3))))) && core.check();
        Assert.assertTrue(nc);

        // x2 <= -4
        nc = core.newClause(new Lit(lra.newLEq(new Lin(x2), new Lin(new Rational(-4))))) && core.check();
        Assert.assertTrue(nc);
        // x2 >= -8
        nc = core.newClause(new Lit(lra.newGEq(new Lin(x2), new Lin(new Rational(-8))))) && core.check();
        Assert.assertTrue(nc);
        // x0 <= 1
        nc = core.newClause(new Lit(lra.newLEq(new Lin(x0), new Lin(ONE)))) && core.check();
        Assert.assertTrue(nc);

        // x1 >= -3
        // this constraint is inconsistent with the previous ones!
        boolean asm = core.assume(new Lit(lra.newGEq(new Lin(x1), new Lin(new Rational(-3)))));
        Assert.assertTrue(asm);
    }
}
