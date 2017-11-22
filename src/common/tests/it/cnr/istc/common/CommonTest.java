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
package it.cnr.istc.common;

import static it.cnr.istc.common.Rational.NEGATIVE_INFINITY;
import static it.cnr.istc.common.Rational.ONE;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class CommonTest {

    @Test
    public void testRational() {
        Rational r0 = new Rational();
        r0.add(ONE);
        r0.add(new Rational(1, 2));
        r0.add(NEGATIVE_INFINITY);
        Rational r1 = new Rational(4, 2);
        Rational r2 = r1.divide(r0);
        r2.add(2);
        r2.sub(-2);
        r2.mult(2);
    }

    @Test
    public void testLin() {
        Lin l0 = new Lin();
        l0.add(0, ONE);
        l0.add(1, new Rational(2));

        Lin l1 = new Lin();
        l1.add(1, ONE);
        l1.add(2, new Rational(2));

        Lin l2 = l0.plus(l1);
    }
}
