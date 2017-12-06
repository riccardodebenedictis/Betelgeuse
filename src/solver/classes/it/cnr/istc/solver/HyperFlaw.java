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
import static it.cnr.istc.smt.LBool.False;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.lra.Rational;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class HyperFlaw extends Flaw {

    final Flaw[] flaws;

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
        Deque<Lit> check_lits = new ArrayDeque<>();
        Deque<Flaw> q = new ArrayDeque<>();
        q.addLast(this);
        while (!q.isEmpty()) {
            Flaw f = q.pollFirst();
            if (slv.sat_core.value(f.getPhi()) == False) {
                return;
            }
            for (Resolver cause : f.causes) {
                check_lits.addLast(new Lit(cause.rho));
                q.addLast(cause.effect);
            }
        }
        for (Resolver[] c_res : new CartesianProductGenerator<>(all_res)) {
            // the resolver's intrinsic cost is given by the maximum of the enclosing resolvers' intrinsic costs..
            Rational cst = Rational.NEGATIVE_INFINITY;
            Lit[] cnj = new Lit[c_res.length];
            for (int i = 0; i < c_res.length; i++) {
                Resolver res = c_res[i];
                cnj[i] = new Lit(res.rho);
                Rational c_cst = res.intrinsic_cost;
                if (c_cst.gt(cst)) {
                    cst = c_cst;
                }
            }
            int cnj_var = slv.sat_core.newConj(cnj);
            check_lits.addLast(new Lit(cnj_var));
            if (slv.sat_core.check(check_lits.toArray(new Lit[check_lits.size()]))) {
                add_resolver(new HyperResolver(slv, cnj_var, cst, this, c_res));
            }
            check_lits.pollLast();
        }
    }

    @Override
    public String getLabel() {
        return "φ" + getPhi() + " hyper-flaw {" + Arrays.stream(flaws).map(lf -> lf.getLabel()).collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public String toString() {
        return "<html>φ" + getPhi() + " hyper-flaw<br>" + Arrays.stream(flaws).map(lf -> lf.toString()).collect(Collectors.joining("<br>")) + "</html>";
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
                    HyperFlaw hf = slv.getHyperFlaw(c_precs);
                    if (hf != null) {
                        slv.newCausalLink(hf, this);
                        slv.setEstimatedCost(this, hf.getEstimatedCost());
                    } else {
                        slv.newFlaw(new HyperFlaw(slv, this, c_precs));
                    }
                }
            } else if (!precs.isEmpty()) {
                // we create a new super flaw including all the preconditions of this resolver..
                Flaw[] c_precs = precs.toArray(new Flaw[precs.size()]);
                HyperFlaw hf = slv.getHyperFlaw(c_precs);
                if (hf != null) {
                    slv.newCausalLink(hf, this);
                    slv.setEstimatedCost(this, hf.getEstimatedCost());
                } else {
                    slv.newFlaw(new HyperFlaw(slv, this, c_precs));
                }
            }
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " hyper-resolver {" + Arrays.stream(ress).map(res -> res.getLabel()).collect(Collectors.joining(", ")) + "}";
        }

        @Override
        public String toString() {
            return "<html>ρ" + rho + " hyper-resolver<br>" + Arrays.stream(ress).map(res -> res.toString()).collect(Collectors.joining("<br>")) + "</html>";
        }
    }
}
