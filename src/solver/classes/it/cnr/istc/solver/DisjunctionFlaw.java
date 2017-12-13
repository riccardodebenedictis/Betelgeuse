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

import it.cnr.istc.core.Conjunction;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Disjunction;
import it.cnr.istc.core.IEnv;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class DisjunctionFlaw extends Flaw {

    private final IEnv env;
    private final Disjunction dsj;

    DisjunctionFlaw(final Solver slv, final Resolver cause, final IEnv env, final Disjunction dsj) {
        super(slv, cause == null ? Collections.emptyList() : Arrays.asList(cause), true, true);
        this.env = env;
        this.dsj = dsj;
    }

    @Override
    protected void compute_resolvers() {
        for (Conjunction cnj : dsj.getConjunctions()) {
            add_resolver(new ChooseConjunction(slv, this, cnj));
        }
    }

    @Override
    public String getLabel() {
        return "φ" + getPhi() + " disj";
    }

    private class ChooseConjunction extends Resolver {

        private final Conjunction cnj;

        private ChooseConjunction(Solver slv, DisjunctionFlaw effect, final Conjunction cnj) {
            super(slv, cnj.getCost(), effect);
            this.cnj = cnj;
        }

        @Override
        protected void expand() throws CoreException {
            cnj.apply(env);
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " conj";
        }
    }
}
