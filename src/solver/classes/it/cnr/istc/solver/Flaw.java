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
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.UnsolvableException;
import it.cnr.istc.smt.Lit;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public abstract class Flaw {

    protected final Solver slv; // the solver this flaw belongs to..
    public final boolean exclusive;
    public final boolean structural;
    private boolean expanded = false;
    private int phi; // the propositional variable indicates whether the flaw is active or not..
    final Collection<Resolver> resolvers = new ArrayList<>(); // the resolvers for this flaw..
    final Collection<Resolver> causes; // the causes for having this flaw..
    final Collection<Resolver> supports = new ArrayList<>(); // the resolvers supported by this flaw..

    public Flaw(final Solver slv, final Collection<Resolver> causes) {
        this(slv, causes, false, false);
    }

    Flaw(final Solver slv, final Collection<Resolver> causes, final boolean exclusive, final boolean structural) {
        this.slv = slv;
        this.causes = causes;
        this.exclusive = exclusive;
        this.structural = structural;
        for (Resolver cause : causes) {
            cause.preconditions.add(this);
        }
    }

    public int getPhi() {
        return phi;
    }

    void init() {
        assert !expanded;
        if (causes.isEmpty()) {
            // the flaw is necessarily active..
            phi = TRUE_var;
        } else {
            // we create a new variable: the flaw is active if all of its causes are active..
            phi = slv.sat_core.newConj(causes.stream().map(res -> new Lit(res.rho)).toArray(Lit[]::new));
        }

        switch (slv.sat_core.value(phi)) {
            case True: // we have a top-level (a landmark) flaw..
                slv.flaws.add(this);
                break;
            case Undefined: // we listen for the flaw to become active..
                Collection<Flaw> fls = slv.phis.get(phi);
                if (fls == null) {
                    fls = new ArrayList<>();
                    slv.phis.put(phi, fls);
                }
                fls.add(this);
                slv.sat_core.bind(phi, slv);
                break;
            default:
                throw new AssertionError(slv.sat_core.value(phi).name());
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    void expand() throws CoreException {
        assert !expanded;

        // we compute the resolvers..
        compute_resolvers();
        expanded = true;

        // we add causal relations between the flaw and its resolvers (i.e., if the flaw is phi exactly one of its resolvers should be in plan)..
        if (resolvers.isEmpty()) {
            // there is no way for solving this flaw..
            if (!slv.sat_core.newClause(new Lit(phi, false))) {
                throw new UnsolvableException();
            }
        } else {
            // we need to take a decision for solving this flaw..
            if (!slv.sat_core.newClause(new Lit(phi, false), new Lit(exclusive ? slv.sat_core.newExctOne(resolvers.stream().map(res -> new Lit(res.rho)).toArray(Lit[]::new)) : slv.sat_core.newDisj(resolvers.stream().map(res -> new Lit(res.rho)).toArray(Lit[]::new)))));
            throw new UnsolvableException();
        }
    }

    abstract void compute_resolvers() throws CoreException;

    protected void add_resolver(Resolver r) {
        resolvers.add(r);
        slv.newResolver(r);
    }

    public Rational getEstimatedCost() {
        return getBestResolver().getEstimatedCost();
    }

    /**
     * Returns the least expensive resolver according to their estimated cost.
     * This method can be overridden in order to further refine the resolver
     * selection procedure.
     *
     * @return the least expensive resolver.
     */
    public Resolver getBestResolver() {
        assert expanded;
        return resolvers.stream().min((Resolver r0, Resolver r1) -> r0.getEstimatedCost().compareTo(r1.getEstimatedCost())).get();
    }
}
