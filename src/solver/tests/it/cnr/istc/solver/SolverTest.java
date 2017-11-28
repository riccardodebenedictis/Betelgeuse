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
package it.cnr.istc.solver;

import it.cnr.istc.common.InfRational;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Item;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class SolverTest {

    @Test
    public void testNewFact() throws CoreException {
        Solver s = new Solver();
        s.read("real a; a >= 5;");

        Item.ArithItem a = s.get("a");
        InfRational a_val = s.value(a);
        Assert.assertTrue(a_val.eq(5));

        s.read("predicate P(int x) {}");
        s.read("goal x = new P();");
    }
}
