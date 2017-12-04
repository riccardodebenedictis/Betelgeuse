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

import it.cnr.istc.common.CartesianProductGenerator;
import it.cnr.istc.common.CombinationGenerator;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.lra.Rational;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class HyperFlaw extends Flaw {

    private final Flaw[] flaws;

    HyperFlaw(Solver slv, final Resolver cause, Flaw... flaws) {
        super(slv, cause == null ? Collections.emptyList() : Arrays.asList(cause));
        this.flaws = flaws;
    }

    @Override
    protected void compute_resolvers() throws CoreException {
        Resolver[][] all_res = new Resolver[flaws.length][];
        for (int i = 0; i < all_res.length; i++) {
            all_res[i] = flaws[i].resolvers.toArray(new Resolver[flaws[i].resolvers.size()]);
        }
        for (Resolver[] c_res : new CartesianProductGenerator<>(all_res)) {
            // the resolver's cost is given by the maximum of the enclosing resolvers' costs..
            Rational cst = Rational.NEGATIVE_INFINITY;
            Lit[] cnj = new Lit[c_res.length];
            for (int i = 0; i < c_res.length; i++) {
                Resolver res = c_res[i];
                cnj[i] = new Lit(res.rho);
                Rational est_cst = res.getEstimatedCost();
                if (est_cst.gt(cst)) {
                    cst = est_cst;
                }
            }
            int cnj_var = slv.sat_core.newConj(cnj);
            if (slv.sat_core.check(new Lit(cnj_var))) {
                add_resolver(new HyperResolver(slv, cnj_var, cst, this, c_res));
            }
        }
    }

    @Override
    public String getLabel() {
        return Arrays.stream(flaws).map(flaw -> flaw.getLabel()).collect(Collectors.joining("\n"));
    }

    public class HyperResolver extends Resolver {

        private final Resolver[] ress;

        private HyperResolver(Solver slv, int rho, Rational cost, Flaw effect, final Resolver[] ress) {
            super(slv, rho, cost, effect);
            this.ress = ress;
        }

        @Override
        protected void expand() throws CoreException {
            Collection<Flaw> precs = new ArrayList<>();
            for (Resolver res : ress) {
                precs.addAll(res.preconditions);
            }

            if (precs.size() > slv.getAccuracy()) {
                // we create sets having the size of the accuracy..
                for (Flaw[] c_precs : new CombinationGenerator<>(slv.getAccuracy(), precs.toArray(new Flaw[precs.size()]))) {
                }
            } else if (!precs.isEmpty()) {
                // we create a new super flaw including all the preconditions of this resolver..
            }
        }

        @Override
        public String getLabel() {
            return Arrays.stream(ress).map(res -> res.getLabel()).collect(Collectors.joining("\n"));
        }
    }
}
