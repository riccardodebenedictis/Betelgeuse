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

import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class CommonTest {

    @Test
    public void testCombinations() {
        Iterator<Character[]> it = new CombinationGenerator<>(2, 'a', 'b', 'c').iterator();
        Assert.assertTrue(it.hasNext());
        Character[] next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "ab");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "ac");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "bc");
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCartesianProduce() {
        Iterator<Character[]> it = new CartesianProductGenerator<>(new Character[]{'a', 'b', 'c'}, new Character[]{'d', 'e'}).iterator();
        Assert.assertTrue(it.hasNext());
        Character[] next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "ad");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "ae");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "bd");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "be");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "cd");
        Assert.assertTrue(it.hasNext());
        next = it.next();
        Assert.assertEquals(new String(new char[]{next[0], next[1]}), "ce");
        Assert.assertFalse(it.hasNext());
    }
}
