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

import it.cnr.istc.common.Rational;
import it.cnr.istc.core.Atom;
import it.cnr.istc.core.Core;
import it.cnr.istc.core.Disjunction;
import it.cnr.istc.core.IEnv;
import it.cnr.istc.core.Item;
import it.cnr.istc.core.Item.VarItem;
import it.cnr.istc.core.Type;
import it.cnr.istc.core.UnsolvableException;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.Theory;
import it.cnr.istc.solver.types.ReusableResource;
import it.cnr.istc.solver.types.StateVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Solver extends Core implements Theory {

    private Resolver res = null;
    private Queue<Flaw> flaw_q = new ArrayDeque<>();
    final Set<Flaw> flaws = new HashSet<>(); // the current active flaws..
    private final Map<Atom, SupportFlaw> reason = new IdentityHashMap<>(); // the reason for having introduced an atom..
    final Map<Integer, Collection<Flaw>> phis = new HashMap<>(); // the phi variables (boolean variable to flaws) of the flaws..
    final Map<Integer, Collection<Resolver>> rhos = new HashMap<>(); // the rho variables (boolean variable to resolvers) of the resolvers..
    private final Deque<Layer> trail = new ArrayDeque<>(); // the list of resolvers in chronological order..
    private final Collection<SolverListener> listeners = new ArrayList<>();

    public Solver() {
        newTypes(new StateVariable(this), new ReusableResource(this));
    }

    @Override
    public Item newEnum(Type type, Set<Item> vals) {
        assert !vals.isEmpty();
        // we create a new enum expression..
        Item c_var = super.newEnum(type, vals);
        if (vals.size() > 1) {
            // we create a new enum flaw..
            newFlaw(new VarFlaw(this, res, (VarItem) c_var));
        }
        return c_var;
    }

    @Override
    protected void newFact(Atom atom) {
        // we create a new support flaw representing a fact..
        SupportFlaw sf = new SupportFlaw(this, res, atom, true);
        reason.put(atom, sf);
        if (atom.type.getScope() != this) {
            Queue<Type> q = new ArrayDeque<>();
            q.add((Type) atom.type.getScope());
            while (!q.isEmpty()) {
                Type tp = q.poll();
                if (tp instanceof SmartType) {
                    ((SmartType) tp).newFact(sf);
                }
                q.addAll(tp.getSupertypes());
            }
        }
    }

    @Override
    protected void newGoal(Atom atom) {
        // we create a new support flaw representing a goal..
        SupportFlaw sf = new SupportFlaw(this, res, atom, false);
        reason.put(atom, sf);
        if (atom.type.getScope() != this) {
            Queue<Type> q = new ArrayDeque<>();
            q.add((Type) atom.type.getScope());
            while (!q.isEmpty()) {
                Type tp = q.poll();
                if (tp instanceof SmartType) {
                    ((SmartType) tp).newGoal(sf);
                }
                q.addAll(tp.getSupertypes());
            }
        }
    }

    @Override
    protected void newDisjunction(IEnv env, Disjunction dsj) {
        // we create a new disjunction flaw..
        newFlaw(new DisjunctionFlaw(this, res, env, dsj));
    }

    public SupportFlaw getReason(final Atom atm) {
        return reason.get(atm);
    }

    private void newFlaw(final Flaw f) {
        f.init(); // flaws' initialization requires being at root-level..
        flaw_q.add(f);
        // we notify the listeners that a new flaw has arised..
        for (SolverListener l : listeners) {
            l.newFlaw(f);
        }
    }

    void newResolver(final Resolver r) {
        r.init(); // resolvers' initialization requires being at root-level..
        // we notify the listeners that a new resolver has arised..
        for (SolverListener l : listeners) {
            l.newResolver(r);
        }
    }

    void newCausalLink(final Flaw f, final Resolver r) throws UnsolvableException {
        r.preconditions.add(f);
        f.supports.add(r);
        if (!sat_core.newClause(new Lit(r.rho, false), new Lit(f.getPhi()))) {
            throw new UnsolvableException();
        }
        // we notify the listeners that a new causal link has been created..
        for (SolverListener l : listeners) {
            l.newCausalLink(f, r);
        }
    }

    void setEstimatedCost(final Resolver r, final Rational cost) {
        if (!r.getEstimatedCost().eq(cost)) {
            if (!trail.isEmpty()) {
                trail.peekLast().old_costs.putIfAbsent(r, r.est_cost);
            }

            // this is the current cost of the resolver's effect..
            Rational f_cost = r.effect.getEstimatedCost();

            // we update the resolver's estimated cost..
            r.est_cost = cost;

            // we notify the listeners that a resolver's cost has changed..
            for (SolverListener l : listeners) {
                l.resolverCostChanged(r);
            }

            // we check if the cost of the resolver's effect has changed as a consequence of the resolver's cost update
            if (!f_cost.eq(r.effect.getEstimatedCost())) {
                // we propagate the update to all the supports of the resolver's effect..
                // the resolver costs queue (for resolver cost propagation)..
                Queue<Resolver> resolver_q = new ArrayDeque<>();
                resolver_q.addAll(r.effect.supports);
                while (!resolver_q.isEmpty()) {
                    Resolver c_res = resolver_q.poll(); // the current resolver whose cost might require an update..
                    Rational c_cost = c_res.preconditions.stream().map(prec -> prec.resolvers.stream().map(prec_res -> prec_res.est_cost).min((Rational r0, Rational r1) -> r0.compareTo(r1)).get()).max((Rational r0, Rational r1) -> r0.compareTo(r1)).get();
                    if (!c_res.est_cost.eq(c_cost)) {
                        if (!trail.isEmpty()) {
                            trail.peekLast().old_costs.putIfAbsent(c_res, c_res.est_cost);
                        }

                        // this is the current cost of the resolver's effect..
                        f_cost = c_res.effect.getEstimatedCost();

                        // we update the resolver's estimated cost..
                        c_res.est_cost = c_cost;

                        // we notify the listeners that a resolver's cost has changed..
                        for (SolverListener l : listeners) {
                            l.resolverCostChanged(r);
                        }

                        // we check if the cost of the resolver's effect has changed as a consequence of the resolver's cost update..
                        if (!f_cost.eq(c_res.effect.getEstimatedCost())) {
                            // we propagate the update to all the supports of the resolver's effect..
                            resolver_q.addAll(c_res.effect.supports);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean propagate(Lit p, Collection<Lit> cnfl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean check(Collection<Lit> cnfl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void push() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void listen(final SolverListener l) {
        listeners.add(l);
    }

    public void forget(final SolverListener l) {
        listeners.remove(l);
    }

    static class Layer {

        private final Resolver res;
        private final Map<Resolver, Rational> old_costs = new IdentityHashMap<>(); // the old estimated resolvers' costs..
        private final Set<Flaw> new_flaws = new HashSet<>(); // the just activated flaws..
        private final Set<Flaw> solved_flaws = new HashSet<>(); // the just activated flaws..

        Layer(Resolver res) {
            this.res = res;
        }
    }
}
