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
class NegationExpression implements Expression {

    private final Expression xpr;

    NegationExpression(final Expression xpr) {
        this.xpr = xpr;
    }

    @Override
    public Item evaluate(IScope scp, IEnv env) throws CoreException {
        return scp.getCore().negate((Item.BoolItem) xpr.evaluate(scp, env));
    }

    @Override
    public String toString() {
        return "!" + xpr.toString();
    }
}
