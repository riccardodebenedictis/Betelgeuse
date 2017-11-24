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

import it.cnr.istc.core.Atom;
import it.cnr.istc.core.Core;
import it.cnr.istc.core.Disjunction;
import it.cnr.istc.core.IEnv;
import it.cnr.istc.solver.types.ReusableResource;
import it.cnr.istc.solver.types.StateVariable;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Solver extends Core {

    public Solver() {
        newTypes(new StateVariable(this), new ReusableResource(this));
    }

    @Override
    protected void newFact(Atom atom) {
        throw new UnsupportedOperationException("not supported yet..");
    }

    @Override
    protected void newGoal(Atom atom) {
        throw new UnsupportedOperationException("not supported yet..");
    }

    @Override
    protected void newDisjunction(IEnv env, Disjunction dsj) {
        throw new UnsupportedOperationException("not supported yet..");
    }
}
