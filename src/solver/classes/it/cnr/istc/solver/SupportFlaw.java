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

import it.cnr.istc.smt.lra.Rational;
import it.cnr.istc.core.Atom;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Predicate;
import it.cnr.istc.core.UnsolvableException;
import static it.cnr.istc.smt.LBool.False;
import static it.cnr.istc.smt.LBool.True;
import static it.cnr.istc.smt.LBool.Undefined;
import it.cnr.istc.smt.Lit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class SupportFlaw extends Flaw {

    public final Atom atom;
    public final boolean is_fact;

    SupportFlaw(final Solver slv, final Resolver cause, final Atom atom, final boolean is_fact) {
        super(slv, cause == null ? Collections.emptyList() : Arrays.asList(cause), true, true);
        this.atom = atom;
        this.is_fact = is_fact;
    }

    @Override
    protected void compute_resolvers() throws UnsolvableException {
        assert slv.sat_core.value(getPhi()) != False;
        assert slv.sat_core.value(atom.sigma) != False;

        // we check if the atom can unify..
        if (slv.sat_core.value(atom.sigma) == Undefined) {
            // we collect the ancestors of this flaw, so as to avoid cyclic causality..
            Set<Flaw> ancestors = new HashSet<>();

            Queue<Flaw> q = new ArrayDeque<>();
            q.add(this);
            while (!q.isEmpty()) {
                Flaw f = q.poll();
                if (ancestors.add(f)) {
                    q.addAll(f.supports.stream().filter(sup -> slv.sat_core.value(sup.rho) != False).map(sup -> sup.effect).collect(Collectors.toList()));
                }
            }

            for (Object inst : atom.type.getInstances()) {
                if (inst == atom) {
                    // the current atom cannot unify with itself..
                    continue;
                }

                // this is the atom we are checking for unification..
                Atom target_atom = (Atom) inst;
                // this is the target flaw (i.e. the one we are checking for unification) and cannot be in the current flaw's causes' effects..
                SupportFlaw target_flaw = slv.getReason(target_atom);

                if (!target_flaw.isExpanded() /* the target flaw must have been already expanded.. */
                        || ancestors.contains(target_flaw) /* unifying with the target atom would introduce cyclic causality.. */
                        || slv.sat_core.value(target_atom.sigma) == False /* the target atom is unified with some other atom.. */
                        || !atom.equates(target_atom) /* the atom does not equate with the target target.. */) {
                    continue;
                }

                // the equality propositional variable..
                int eq_v = atom.eq(target_atom);

                if (slv.sat_core.value(eq_v) == False) {
                    // the two atoms cannot unify, hence, we skip this instance..
                    continue;
                }

                // since atom 'target_atom' is a good candidate for unification, we build the unification literals..
                Collection<Lit> unif_lits = new ArrayList<>();
                q.add(this);
                q.add(target_flaw);
                unif_lits.add(new Lit(atom.sigma, false)); // we force the state of this atom to be 'unified' within the unification literals..
                unif_lits.add(new Lit(target_atom.sigma)); // we force the state of the target atom to be 'active' within the unification literals..
                Set<Flaw> seen = new HashSet<>(); // we avoid some repetition of literals..
                while (!q.isEmpty()) {
                    Flaw f = q.poll();
                    if (seen.add(f)) {
                        for (Resolver cause : f.causes) {
                            if (slv.sat_core.value(cause.rho) != True) {
                                unif_lits.add(new Lit(cause.rho)); // we add the resolver's variable to the unification literals..
                                q.add(cause.effect); // we push its effect..
                            }
                        }
                    }
                }

                if (slv.sat_core.value(eq_v) != True) {
                    unif_lits.add(new Lit(eq_v));
                }

                Lit[] unif_lits_arr = unif_lits.toArray(new Lit[unif_lits.size()]);
                if (slv.sat_core.check(unif_lits_arr)) {
                    // unification is actually possible!
                    Unify unify = new Unify(slv, this, atom, target_atom, unif_lits_arr);
                    assert slv.sat_core.value(unify.rho) != False;
                    add_resolver(unify);
                    slv.newCausalLink(this, unify);
                    slv.setEstimatedCost(unify, unify.cost);
                }
            }
        }

        if (is_fact) {
            add_resolver(new ActivateFact(slv, this, atom));
        } else {
            add_resolver(new ActivateGoal(slv, this, atom));
        }
    }

    @Override
    public String getLabel() {
        return "φ" + getPhi() + (is_fact ? " fact σ" : " goal σ") + atom.sigma + " " + atom.type.name;
    }

    public class ActivateFact extends Resolver {

        private final Atom atom;

        private ActivateFact(Solver slv, SupportFlaw effect, final Atom atom) {
            super(slv, new Rational(), effect);
            this.atom = atom;
        }

        @Override
        protected void expand() throws CoreException {
            if (!slv.sat_core.newClause(new Lit(rho, false), new Lit(atom.sigma))) {
                throw new UnsolvableException();
            }
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " activate fact";
        }
    }

    public class ActivateGoal extends Resolver {

        private final Atom atom;

        private ActivateGoal(Solver slv, SupportFlaw effect, final Atom atom) {
            super(slv, new Rational(1), effect);
            this.atom = atom;
        }

        @Override
        protected void expand() throws CoreException {
            if (!slv.sat_core.newClause(new Lit(rho, false), new Lit(atom.sigma))) {
                throw new UnsolvableException();
            }
            ((Predicate) atom.type).applyRule(atom);
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " expand goal";
        }
    }

    private class Unify extends Resolver {

        private final Atom source;
        private final Atom target;
        private final Lit[] unif_lits;

        private Unify(Solver slv, SupportFlaw effect, final Atom source, final Atom target, final Lit[] unif_lits) {
            super(slv, new Rational(1), effect);
            this.source = source;
            this.target = target;
            this.unif_lits = unif_lits;
        }

        @Override
        protected void expand() throws CoreException {
            for (Lit l : unif_lits) {
                if (!slv.sat_core.newClause(new Lit(rho, false), l)) {
                    throw new UnsolvableException();
                }
            }
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " unify";
        }
    }
}
