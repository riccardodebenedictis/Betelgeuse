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
    }

    void expand() {
    }

    abstract void compute_resolvers();

    protected void add_resolver(Resolver r) {
    }

    /**
     * Returns the least expensive resolver according to their estimated cost.
     * This method can be extended in order to further refine the resolver
     * selection procedure.
     *
     * @return the least expensive resolver.
     */
    public Resolver getBestResolver() {
        assert expanded;
        return resolvers.stream().min((Resolver r0, Resolver r1) -> Double.compare(r0.est_cost, r1.est_cost)).get();
    }
}
