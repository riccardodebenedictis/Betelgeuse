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
public class GtExpression implements Expression {

    private final Expression xpr0;
    private final Expression xpr1;

    public GtExpression(final Expression xpr0, final Expression xpr1) {
        this.xpr0 = xpr0;
        this.xpr1 = xpr1;
    }

    @Override
    public Item evaluate(IScope scp, IEnv env) throws CoreException {
        return scp.getCore().gt((Item.ArithItem) xpr0.evaluate(scp, env), (Item.ArithItem) xpr1.evaluate(scp, env));
    }

    @Override
    public String toString() {
        return xpr0.toString() + " > " + xpr1.toString();
    }
}
