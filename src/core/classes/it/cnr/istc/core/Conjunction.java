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

import it.cnr.istc.smt.lra.Rational;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Conjunction extends Scope {

    private final Rational cost;
    private final List<Statement> statements;

    Conjunction(final Core core, final IScope scope, final Rational cost, final List<Statement> statements) {
        super(core, scope);
        this.cost = cost;
        this.statements = statements;
    }

    public Rational getCost() {
        return new Rational(cost);
    }

    public void apply(final IEnv env) throws CoreException {
        IEnv c_env = new Env(core, env);
        for (Statement stmnt : statements) {
            stmnt.execute(this, c_env);
        }
    }
}
