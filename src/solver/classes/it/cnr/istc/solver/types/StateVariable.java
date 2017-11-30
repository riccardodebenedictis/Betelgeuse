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
package it.cnr.istc.solver.types;

import it.cnr.istc.core.Atom;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Field;
import static it.cnr.istc.core.IScope.TAU;
import it.cnr.istc.core.Predicate;
import it.cnr.istc.core.Type;
import it.cnr.istc.solver.Flaw;
import it.cnr.istc.solver.SmartType;
import it.cnr.istc.solver.Solver;
import it.cnr.istc.solver.SupportFlaw;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class StateVariable extends SmartType {

    public static final String STATE_VARIABLE = "StateVariable";

    public StateVariable(final Solver slv) {
        super(slv, slv, STATE_VARIABLE);
    }

    @Override
    protected void newPredicate(Predicate p) {
        Type.newSupertypes(p, core.getPredicate("IntervalPredicate"));
        newFields(p, new Field(this, TAU));
    }

    @Override
    public Collection<Flaw> getFlaws() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class SVFlaw extends Flaw {

        private SVFlaw(Solver slv, Collection<Atom> overlapping_atoms) {
            super(slv, overlapping_atoms.stream().flatMap(atom -> slv.getReason(atom).getResolvers().stream()).filter(res -> (res instanceof SupportFlaw.ActivateFact) || (res instanceof SupportFlaw.ActivateGoal)).collect(Collectors.toList()));
        }

        @Override
        protected void compute_resolvers() throws CoreException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLabel() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static class SVAtomListener extends AtomListener {

        private SVAtomListener(Atom atom) {
            super(atom);
        }

        @Override
        public void satValueChange(int v) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void lraValueChange(int v) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void varValueChange(int v) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
