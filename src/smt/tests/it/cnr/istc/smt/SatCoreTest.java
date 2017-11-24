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
package it.cnr.istc.smt;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class SatCoreTest {

    @Test
    public void testSatCore() {
        SatCore core = new SatCore();

        int b0 = core.newVar();
        int b1 = core.newVar();
        int b2 = core.newVar();

        boolean nc = core.newClause(new Lit(b0, false), new Lit(b1, false), new Lit(b2));
        Assert.assertTrue(nc);
        boolean ch = core.check();
        Assert.assertTrue(ch);
        Assert.assertEquals(core.value(b0), LBool.Undefined);
        Assert.assertEquals(core.value(b1), LBool.Undefined);
        Assert.assertEquals(core.value(b2), LBool.Undefined);

        boolean asm = core.assume(new Lit(b0)) && core.check();
        Assert.assertTrue(asm);
        Assert.assertEquals(core.value(b0), LBool.True);
        Assert.assertEquals(core.value(b1), LBool.Undefined);
        Assert.assertEquals(core.value(b2), LBool.Undefined);

        asm = core.assume(new Lit(b1)) && core.check();
        Assert.assertTrue(asm);
        Assert.assertEquals(core.value(b0), LBool.True);
        Assert.assertEquals(core.value(b1), LBool.True);
        Assert.assertEquals(core.value(b2), LBool.True);
    }

    @Test
    public void testNoGood() {
        SatCore core = new SatCore();

        int b0 = core.newVar();
        int b1 = core.newVar();
        int b2 = core.newVar();
        int b3 = core.newVar();
        int b4 = core.newVar();
        int b5 = core.newVar();
        int b6 = core.newVar();
        int b7 = core.newVar();
        int b8 = core.newVar();

        boolean nc = core.newClause(new Lit(b0), new Lit(b1));
        Assert.assertTrue(nc);
        nc = core.newClause(new Lit(b0), new Lit(b2), new Lit(b6));
        Assert.assertTrue(nc);
        nc = core.newClause(new Lit(b1, false), new Lit(b2, false), new Lit(b3));
        Assert.assertTrue(nc);
        nc = core.newClause(new Lit(b3, false), new Lit(b4), new Lit(b7));
        Assert.assertTrue(nc);
        nc = core.newClause(new Lit(b3, false), new Lit(b5), new Lit(b8));
        Assert.assertTrue(nc);
        nc = core.newClause(new Lit(b4, false), new Lit(b5, false));
        Assert.assertTrue(nc);

        boolean asm = core.assume(new Lit(b6, false)) && core.check();
        Assert.assertTrue(asm);
        asm = core.assume(new Lit(b7, false)) && core.check();
        Assert.assertTrue(asm);
        asm = core.assume(new Lit(b8, false)) && core.check();
        Assert.assertTrue(asm);
        asm = core.assume(new Lit(b0, false)) && core.check();
        Assert.assertTrue(asm);
    }
}
