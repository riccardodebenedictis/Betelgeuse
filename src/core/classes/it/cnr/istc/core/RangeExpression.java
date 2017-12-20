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

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class RangeExpression implements Expression {

    private final Expression min_e;
    private final Expression max_e;

    public RangeExpression(final Expression min_e, final Expression max_e) {
        this.min_e = min_e;
        this.max_e = max_e;
    }

    @Override
    public Item evaluate(IScope scp, IEnv env) throws CoreException {
        Item.ArithItem min_v = (Item.ArithItem) min_e.evaluate(scp, env);
        Item.ArithItem max_v = (Item.ArithItem) max_e.evaluate(scp, env);
        Item.ArithItem var = (min_v.type.name.equals(Type.REAL) || max_v.type.name.equals(Type.REAL)) ? scp.getCore().newReal() : scp.getCore().newInt();
        scp.getCore().assertFacts(scp.getCore().geq(var, min_v).l, scp.getCore().leq(var, max_v).l);
        return var;
    }

    @Override
    public String toString() {
        return "[" + min_e.toString() + ", " + max_e.toString() + "]";
    }
}
