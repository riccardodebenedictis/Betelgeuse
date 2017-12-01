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
import static it.cnr.istc.smt.lra.Rational.POSITIVE_INFINITY;
import static it.cnr.istc.smt.lra.Rational.ZERO;
import it.cnr.istc.core.Atom;
import it.cnr.istc.core.Core;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Disjunction;
import it.cnr.istc.core.IEnv;
import it.cnr.istc.core.InconsistencyException;
import it.cnr.istc.core.Item;
import it.cnr.istc.core.Item.VarItem;
import it.cnr.istc.core.Type;
import it.cnr.istc.core.UnsolvableException;
import static it.cnr.istc.smt.LBool.False;
import static it.cnr.istc.smt.LBool.True;
import static it.cnr.istc.smt.LBool.Undefined;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.Theory;
import it.cnr.istc.solver.types.ReusableResource;
import it.cnr.istc.solver.types.StateVariable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Solver extends Core implements Theory {

    private Resolver res = null;
    private int gamma; // this variable represents the validity of the current graph..
    private Queue<Flaw> flaw_q = new ArrayDeque<>();
    final Set<Flaw> flaws = new HashSet<>(); // the current active flaws..
    private final Map<Atom, SupportFlaw> reason = new IdentityHashMap<>(); // the reason for having introduced an atom..
    final Map<Integer, Collection<Flaw>> phis = new HashMap<>(); // the phi variables (boolean variable to flaws) of the flaws..
    final Map<Integer, Collection<Resolver>> rhos = new HashMap<>(); // the rho variables (boolean variable to resolvers) of the resolvers..
    private final Deque<Layer> trail = new ArrayDeque<>(); // the list of resolvers in chronological order..
    private final Collection<SolverListener> listeners = new ArrayList<>();

    public void init() {
        try {
            read(new FileReader(new File(Solver.class.getResource("init.rddl").toURI())));
            newTypes(new StateVariable(this), new ReusableResource(this));
        } catch (CoreException | FileNotFoundException | URISyntaxException ex) {
            throw new AssertionError("something went wrong..", ex);
        }
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
    protected void newFact(Atom atom) throws CoreException {
        // we create a new support flaw representing a fact..
        SupportFlaw sf = new SupportFlaw(this, res, atom, true);
        reason.put(atom, sf);
        newFlaw(sf);
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
    protected void newGoal(Atom atom) throws CoreException {
        // we create a new support flaw representing a goal..
        SupportFlaw sf = new SupportFlaw(this, res, atom, false);
        reason.put(atom, sf);
        newFlaw(sf);
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

    public void solve() throws CoreException {
        // we build the causal graph..
        build();

        while (sat_core.rootLevel()) {
            // we have exhausted the search within the graph: we extend the graph..
            add_layer();
        }

        while (true) {
            // this is the next flaw to be solved..
            Flaw f_next = select_flaw();
            if (f_next != null) {
                for (SolverListener l : listeners) {
                    l.currentFlaw(f_next);
                }
                assert !f_next.getEstimatedCost().isPositiveInfinite();
                if (!f_next.structural && !hasInconsistencies()) { // we run out of structural flaws, thus, we renew them..
                    // this is the next resolver to be assumed..
                    res = f_next.getBestResolver();

                    // we apply the resolver..
                    if (!sat_core.assume(new Lit(res.rho)) || !sat_core.check()) {
                        throw new UnsolvableException();
                    }

                    res = null;
                    while (sat_core.rootLevel()) {
                        if (sat_core.value(gamma) == Undefined) {
                            // we have learnt a unit clause! thus, we reassume the graph var and restart the search..
                            if (!sat_core.assume(new Lit(gamma)) || !sat_core.check()) {
                                throw new UnsolvableException();
                            }
                        } else {
                            // we have exhausted the search within the graph: we extend the graph..
                            assert sat_core.value(gamma) == False;
                            add_layer();
                        }
                    }
                }
            } else if (!hasInconsistencies()) { // we run out of structural flaws, we check for inconsistencies one last time..
                // Hurray!! we have found a solution..
                return;
            }
        }
    }

    private void build() throws CoreException {
        while (flaws.stream().anyMatch(flaw -> flaw.getEstimatedCost().isPositiveInfinite())) {
            if (flaw_q.isEmpty()) {
                throw new UnsolvableException("no more flaws to expand..");
            }
            Queue<Flaw> c_q = flaw_q;
            flaw_q = new ArrayDeque<>();
            for (Flaw flaw : c_q) {
                expandFlaw(flaw);
            }
        }

        // we create a new graph var..
        gamma = sat_core.newVar();
        // these flaws have not been expanded, hence, cannot have a solution..
        for (Flaw flaw : flaw_q) {
            if (!sat_core.newClause(new Lit(gamma, false), new Lit(flaw.getPhi(), false))) {
                throw new UnsolvableException();
            }
        }
        // we assume the new graph var to allow search within the current graph..
        if (!sat_core.assume(new Lit(gamma)) || !sat_core.check()) {
            throw new UnsolvableException();
        }
    }

    private void add_layer() throws CoreException {
        Collection<Flaw> fringe = new ArrayList<>(flaw_q);
        while (fringe.stream().allMatch(flaw -> flaw.getEstimatedCost().isPositiveInfinite())) {
            if (flaw_q.isEmpty()) {
                throw new UnsolvableException("no more flaws to expand..");
            }
            Queue<Flaw> c_q = flaw_q;
            flaw_q = new ArrayDeque<>();
            for (Flaw flaw : c_q) {
                expandFlaw(flaw);
            }
        }

        // we create a new graph var..
        gamma = sat_core.newVar();
        // these flaws have not been expanded, hence, cannot have a solution..
        for (Flaw flaw : flaw_q) {
            if (!sat_core.newClause(new Lit(gamma, false), new Lit(flaw.getPhi(), false))) {
                throw new UnsolvableException();
            }
        }
        // we assume the new graph var to allow search within the current graph..
        if (!sat_core.assume(new Lit(gamma)) || !sat_core.check()) {
            throw new UnsolvableException();
        }
    }

    private void expandFlaw(final Flaw f) throws CoreException {
        assert !f.isExpanded();
        // we expand the flaw..
        f.expand();
        if (!sat_core.check()) {
            throw new UnsolvableException();
        }
        for (Resolver r : f.resolvers) {
            res = r;
            setVar(r.rho);
            try {
                r.expand();
            } catch (InconsistencyException e) {
                if (!sat_core.newClause(new Lit(r.rho, false))) {
                    throw new UnsolvableException();
                }
            }
            if (!sat_core.check()) {
                throw new UnsolvableException();
            }
            restoreVar();
            res = null;
            if (r.preconditions.isEmpty() && sat_core.value(r.rho) != False) {
                setEstimatedCost(r, ZERO);
            }
        }
    }

    private boolean hasInconsistencies() throws CoreException {
        Collection<Flaw> incs = new ArrayList<>();
        Queue<Type> q = new ArrayDeque<>();
        q.addAll(getTypes().values());
        while (!q.isEmpty()) {
            Type tp = q.poll();
            if (tp instanceof SmartType) {
                incs.addAll(((SmartType) tp).getFlaws());
            }
            q.addAll(tp.getTypes().values());
        }

        if (incs.isEmpty()) {
            return false;
        } else {
            // we go back to root level..
            while (!sat_core.rootLevel()) {
                sat_core.pop();
            }
            // we initialize and expand the new flaws..
            for (Flaw f : incs) {
                f.init();
                // we notify the listeners that a new flaw has arised..
                for (SolverListener l : listeners) {
                    l.newFlaw(f);
                }
                expandFlaw(f);
            }
            // we re-assume the current graph var to allow search within the current graph..
            if (!sat_core.assume(new Lit(gamma)) || !sat_core.check()) {
                throw new UnsolvableException();
            }
            return true;
        }
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

    private Flaw select_flaw() {
        assert flaws.stream().allMatch(flaw -> flaw.isExpanded() && sat_core.value(flaw.getPhi()) == True);
        // this is the next flaw to be solved (i.e., the most expensive one)..
        Flaw f_next = null;
        Iterator<Flaw> f_it = flaws.iterator();
        while (f_it.hasNext()) {
            Flaw next = f_it.next();
            if (next.resolvers.stream().anyMatch(res -> sat_core.value(res.rho) == True)) {
                // we have either a trivial (i.e. has only one resolver) or an already solved flaw..
                if (!trail.isEmpty()) {
                    trail.peekLast().solved_flaws.add(next);
                }
                f_it.remove();
            } else {
                // the current flaw is not trivial nor already solved: let's see if it's better than the previous one..
                if (f_next == null /* this is the first flaw we see.. */
                        || f_next.structural && !next.structural /* we prefere non-structural flaws (i.e., inconsistencies) to structural ones.. */
                        || f_next.structural == next.structural && f_next.getEstimatedCost().lt(next.getEstimatedCost()) /* this flaw is actually better than the previous one.. */) {
                    f_next = next;
                }
            }
        }
        return f_next;
    }

    @Override
    public boolean propagate(Lit p, Collection<Lit> cnfl) {
        assert cnfl.isEmpty();

        if (res == null) {
            Collection<Flaw> fs = phis.get(p.v);
            if (fs != null) {
                // a decision has been taken about the presence of some flaws within the current partial solution..
                for (Flaw f : fs) {
                    assert !flaws.contains(f);
                    if (p.sign) {
                        // this flaw has been added to the current partial solution..
                        flaws.add(f);
                        if (!trail.isEmpty()) {
                            trail.peekLast().new_flaws.add(f);
                        }
                        // we notify the listeners that the state of the flaw has changed..
                        for (SolverListener l : listeners) {
                            l.flawStateChanged(f);
                        }
                    }
                }
            }

            Collection<Resolver> rs;
            if (p.sign && (rs = rhos.get(p.v)) != null) {
                for (Resolver r : rs) {
                    setEstimatedCost(r, POSITIVE_INFINITY);
                }
            }
        }

        return true;
    }

    @Override
    public boolean check(Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        return true;
    }

    @Override
    public void push() {
        if (res != null) {
            Layer layer = new Layer(res);
            layer.solved_flaws.add(res.effect);
            flaws.remove(res.effect);
            trail.push(layer);
        }
    }

    @Override
    public void pop() {
        if (res != null) {
            // we reintroduce the solved flaw..
            Layer layer = trail.pop();
            for (SolverListener l : listeners) {
                l.currentResolver(layer.res);
            }
            // we reintroduce the solved flaw..
            flaws.addAll(layer.solved_flaws);
            // we erase the new flaws..
            flaws.removeAll(layer.new_flaws);
            // we restore the resolvers' estimated costs..
            for (Map.Entry<Resolver, Rational> cost : layer.old_costs.entrySet()) {
                cost.getKey().est_cost = cost.getValue();
            }
            // we notify the listeners that the cost of some resolvers has been restored..
            for (SolverListener l : listeners) {
                for (Map.Entry<Resolver, Rational> cost : layer.old_costs.entrySet()) {
                    l.resolverCostChanged(cost.getKey());
                }
            }
        }
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
