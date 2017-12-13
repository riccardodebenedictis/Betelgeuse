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
class ReturnStatement implements Statement {

    private final Expression xpr;

    ReturnStatement(final Expression xpr) {
        this.xpr = xpr;
    }

    @Override
    public void execute(IScope scp, IEnv env) throws CoreException {
        if (env instanceof Core) {
            ((Core) env).items.put(IScope.RETURN, xpr.evaluate(scp, env));
        } else {
            ((Env) env).items.put(IScope.RETURN, xpr.evaluate(scp, env));
        }
    }

    @Override
    public String toString() {
        return "return " + xpr.toString() + ";";
    }
}
