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
package it.cnr.istc.smt.lra;

import it.cnr.istc.common.InfRational;
import it.cnr.istc.common.Lin;
import it.cnr.istc.common.Rational;
import static it.cnr.istc.common.Rational.NEGATIVE_INFINITY;
import static it.cnr.istc.common.Rational.ONE;
import static it.cnr.istc.common.Rational.POSITIVE_INFINITY;
import static it.cnr.istc.common.Rational.ZERO;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import static it.cnr.istc.smt.SatCore.FALSE_var;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import it.cnr.istc.smt.Theory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class LRATheory implements Theory {

    final SatCore sat_core;
    final List<Bound> assigns = new ArrayList<>(); // the current assignments..
    private final List<InfRational> vals = new ArrayList<>(); // the current values..
    private final Map<Integer, Row> tableau = new TreeMap<>(); // the sparse matrix..
    private final Map<String, Integer> exprs = new HashMap<>(); // the expressions (string to numeric variable) for which already exist slack variables..
    private final Map<String, Integer> s_asrts = new HashMap<>(); // the assertions (string to boolean variable) used for reducing the number of boolean variables..
    private final Map<Integer, Assertion> v_asrts = new HashMap<>(); // the assertions (boolean variable to assertion) used for enforcing (negating) assertions..
    final List<Collection<Assertion>> a_watches = new ArrayList<>(); // for each variable 'v', a list of assertions watching 'v'..
    private final List<Set<Row>> t_watches = new ArrayList<>(); // for each variable 'v', a list of tableau rows watching 'v'..
    private final Deque<Map<Integer, Bound>> layers = new ArrayDeque<>(); // we store the updated bounds..
    private final Map<Integer, Collection<LRAValueListener>> listeners = new HashMap<>();

    public LRATheory(final SatCore core) {
        this.sat_core = core;
        core.addTheory(this);
    }

    public int newVar() {
        final int id = vals.size();
        assigns.add(new Bound(new InfRational(NEGATIVE_INFINITY), null));
        assigns.add(new Bound(new InfRational(POSITIVE_INFINITY), null));
        vals.add(new InfRational(ZERO));
        exprs.put("x" + id, id);
        a_watches.add(new ArrayList<>());
        t_watches.add(new HashSet<>());
        return id;
    }

    public int newLt(final Lin left, final Lin right) {
        Lin expr = left.minus(right);
        for (Integer v : new ArrayList<>(expr.vars.keySet())) {
            Row row = tableau.get(v);
            if (row != null) {
                expr.add(row.l.times(expr.vars.remove(v)));
            }
        }

        final InfRational c_right = new InfRational(expr.known_term.minus(), -1);
        expr.known_term = new Rational();

        if (ub(expr).leq(c_right)) {
            return TRUE_var; // the constraint is already satisfied..
        } else if (lb(expr).gt(c_right)) {
            return FALSE_var; // the constraint is unsatisfable..
        }

        final int slack = mk_slack(expr);
        final String s_assertion = "x" + slack + " <= " + c_right.toString();
        final Integer asrt_var = s_asrts.get(s_assertion);
        if (asrt_var != null) {
            return asrt_var; // this assertion already exists..
        } else {
            final int ctr = sat_core.newVar();
            sat_core.bind(ctr, this);
            s_asrts.put(s_assertion, ctr);
            v_asrts.put(ctr, new Assertion(this, Assertion.Op.LEq, ctr, slack, c_right));
            return ctr;
        }
    }

    public int newLEq(final Lin left, final Lin right) {
        Lin expr = left.minus(right);
        for (Integer v : new ArrayList<>(expr.vars.keySet())) {
            Row row = tableau.get(v);
            if (row != null) {
                expr.add(row.l.times(expr.vars.remove(v)));
            }
        }

        final InfRational c_right = new InfRational(expr.known_term.minus());
        expr.known_term = new Rational();

        if (ub(expr).leq(c_right)) {
            return TRUE_var; // the constraint is already satisfied..
        } else if (lb(expr).gt(c_right)) {
            return FALSE_var; // the constraint is unsatisfable..
        }

        final int slack = mk_slack(expr);
        final String s_assertion = "x" + slack + " <= " + c_right.toString();
        final Integer asrt_var = s_asrts.get(s_assertion);
        if (asrt_var != null) {
            return asrt_var; // this assertion already exists..
        } else {
            final int ctr = sat_core.newVar();
            sat_core.bind(ctr, this);
            s_asrts.put(s_assertion, ctr);
            v_asrts.put(ctr, new Assertion(this, Assertion.Op.LEq, ctr, slack, c_right));
            return ctr;
        }
    }

    public int newGEq(final Lin left, final Lin right) {
        Lin expr = left.minus(right);
        for (Integer v : new ArrayList<>(expr.vars.keySet())) {
            Row row = tableau.get(v);
            if (row != null) {
                expr.add(row.l.times(expr.vars.remove(v)));
            }
        }

        final InfRational c_right = new InfRational(expr.known_term.minus());
        expr.known_term = new Rational();

        if (lb(expr).geq(c_right)) {
            return TRUE_var; // the constraint is already satisfied..
        } else if (ub(expr).lt(c_right)) {
            return FALSE_var; // the constraint is unsatisfable..
        }

        final int slack = mk_slack(expr);
        final String s_assertion = "x" + slack + " >= " + c_right.toString();
        final Integer asrt_var = s_asrts.get(s_assertion);
        if (asrt_var != null) {
            return asrt_var; // this assertion already exists..
        } else {
            final int ctr = sat_core.newVar();
            sat_core.bind(ctr, this);
            s_asrts.put(s_assertion, ctr);
            v_asrts.put(ctr, new Assertion(this, Assertion.Op.GEq, ctr, slack, c_right));
            return ctr;
        }
    }

    public int newGt(final Lin left, final Lin right) {
        Lin expr = left.minus(right);
        for (Integer v : new ArrayList<>(expr.vars.keySet())) {
            Row row = tableau.get(v);
            if (row != null) {
                expr.add(row.l.times(expr.vars.remove(v)));
            }
        }

        final InfRational c_right = new InfRational(expr.known_term.minus(), 1);
        expr.known_term = new Rational();

        if (lb(expr).geq(c_right)) {
            return TRUE_var; // the constraint is already satisfied..
        } else if (ub(expr).lt(c_right)) {
            return FALSE_var; // the constraint is unsatisfable..
        }

        final int slack = mk_slack(expr);
        final String s_assertion = "x" + slack + " >= " + c_right.toString();
        final Integer asrt_var = s_asrts.get(s_assertion);
        if (asrt_var != null) {
            return asrt_var; // this assertion already exists..
        } else {
            final int ctr = sat_core.newVar();
            sat_core.bind(ctr, this);
            s_asrts.put(s_assertion, ctr);
            v_asrts.put(ctr, new Assertion(this, Assertion.Op.GEq, ctr, slack, c_right));
            return ctr;
        }
    }

    private int mk_slack(final Lin l) {
        final String s_expr = l.toString();
        Integer sl_expr = exprs.get(s_expr);
        if (sl_expr != null) {
            return sl_expr; // the expression already exists..
        } else {
            // we need to create a new slack variable..
            final int slack = newVar();
            exprs.put(s_expr, slack);
            vals.set(slack, value(l)); // we set the initial value of the new slack variable..
            tableau.put(slack, new Row(this, slack, l)); // we add a new row into the tableau..
            return slack;
        }
    }

    /**
     * Returns the current lower bound of variable 'v'.
     *
     * @param v the variable whose lower bound we are interested in.
     * @return the lower bound of variable 'v'.
     */
    public InfRational lb(final int v) {
        return assigns.get(lb_index(v)).value;
    }

    /**
     * Returns the current upper bound of variable 'v'.
     *
     * @param v the variable whose upper bound we are interested in.
     * @return the upper bound of variable 'v'.
     */
    public InfRational ub(final int v) {
        return assigns.get(ub_index(v)).value;
    }

    /**
     * Returns the current value of variable 'v'.
     *
     * @param v the variable whose value we are interested in.
     * @return the value of variable 'v'.
     */
    public InfRational value(final int v) {
        return vals.get(v);
    }

    /**
     * Returns the current lower bound of linear expression 'l'.
     *
     * @param l the linear expression whose lower bound we are interested in.
     * @return the lower bound of linear expression 'l'.
     */
    public InfRational lb(final Lin l) {
        InfRational v = new InfRational(l.known_term);
        for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
            v.add((term.getValue().isPositive() ? lb(term.getKey()) : ub(term.getKey())).times(term.getValue()));
        }
        return v;
    }

    /**
     * Returns the current upper bound of linear expression 'l'.
     *
     * @param l the linear expression whose upper bound we are interested in.
     * @return the upper bound of linear expression 'l'.
     */
    public InfRational ub(final Lin l) {
        InfRational v = new InfRational(l.known_term);
        for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
            v.add((term.getValue().isPositive() ? ub(term.getKey()) : lb(term.getKey())).times(term.getValue()));
        }
        return v;
    }

    /**
     * Returns the current value of linear expression 'l'.
     *
     * @param l the linear expression whose value we are interested in.
     * @return the value of linear expression 'l'.
     */
    public InfRational value(final Lin l) {
        InfRational v = new InfRational(l.known_term);
        for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
            v.add(value(term.getKey()).times(term.getValue()));
        }
        return v;
    }

    @Override
    public boolean propagate(Lit p, Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        Assertion a = v_asrts.get(p.v);
        switch (a.op) {
            case LEq:
                return p.sign ? assert_upper(a.x, a.v, p, cnfl) : assert_lower(a.x, a.v, p, cnfl);
            case GEq:
                return p.sign ? assert_lower(a.x, a.v, p, cnfl) : assert_upper(a.x, a.v, p, cnfl);
            default:
                throw new AssertionError(a.op.name());
        }
    }

    @Override
    public boolean check(Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        while (true) {
            // we find a basic variable whose value is outside its bounds..
            Optional<Map.Entry<Integer, Row>> x_i = tableau.entrySet().stream().filter(row -> value(row.getKey()).lt(lb(row.getKey())) || value(row.getKey()).gt(ub(row.getKey()))).findFirst();
            if (!x_i.isPresent()) {
                return true;
            }
            if (value(x_i.get().getKey()).lt(lb(x_i.get().getKey()))) {
                // the current value is lower than the lower bound..
                Optional<Map.Entry<Integer, Rational>> x_j = x_i.get().getValue().l.vars.entrySet().stream().filter(term -> (term.getValue().isPositive() && value(term.getKey()).lt(ub(term.getKey()))) || (term.getValue().isNegative() && value(term.getKey()).gt(lb(term.getKey())))).findFirst();
                if (x_j.isPresent()) {
                    // var x_j can be used to increase the value of x_i..
                    pivot_and_update(x_i.get().getKey(), x_j.get().getKey(), lb(x_i.get().getKey()));
                } else {
                    // we generate an explanation for the conflict..
                    for (Map.Entry<Integer, Rational> term : x_i.get().getValue().l.vars.entrySet()) {
                        if (term.getValue().isPositive()) {
                            cnfl.add(assigns.get(ub_index(term.getKey())).reason.not());
                        } else if (term.getValue().isNegative()) {
                            cnfl.add(assigns.get(lb_index(term.getKey())).reason.not());
                        }
                    }
                    cnfl.add(assigns.get(lb_index(x_i.get().getKey())).reason.not());
                    return false;
                }
            }
            if (value(x_i.get().getKey()).gt(ub(x_i.get().getKey()))) {
                // the current value is greater than the upper bound..
                Optional<Map.Entry<Integer, Rational>> x_j = x_i.get().getValue().l.vars.entrySet().stream().filter(term -> (term.getValue().isNegative() && value(term.getKey()).lt(ub(term.getKey()))) || (term.getValue().isPositive() && value(term.getKey()).gt(lb(term.getKey())))).findFirst();
                if (x_j.isPresent()) {
                    // var x_j can be used to decrease the value of x_i..
                    pivot_and_update(x_i.get().getKey(), x_j.get().getKey(), ub(x_i.get().getKey()));
                } else {
                    // we generate an explanation for the conflict..
                    for (Map.Entry<Integer, Rational> term : x_i.get().getValue().l.vars.entrySet()) {
                        if (term.getValue().isPositive()) {
                            cnfl.add(assigns.get(lb_index(term.getKey())).reason.not());
                        } else if (term.getValue().isNegative()) {
                            cnfl.add(assigns.get(ub_index(term.getKey())).reason.not());
                        }
                    }
                    cnfl.add(assigns.get(ub_index(x_i.get().getKey())).reason.not());
                    return false;
                }
            }
        }
    }

    @Override
    public void push() {
        layers.add(new HashMap<>());
    }

    @Override
    public void pop() {
        // we restore the variables' bounds and their reason..
        for (Map.Entry<Integer, Bound> bound : layers.pollLast().entrySet()) {
            assigns.set(bound.getKey(), bound.getValue());
        }
    }

    private boolean assert_lower(final int x_i, final InfRational val, final Lit p, final Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        if (val.leq(lb(x_i))) {
            return true;
        } else if (val.gt(ub(x_i))) {
            cnfl.add(p.not());                                 // either the literal 'p' is false ..
            cnfl.add(assigns.get(ub_index(x_i)).reason.not()); // or what asserted the upper bound is false..
            return false;
        } else {
            if (!layers.isEmpty() && !layers.getLast().containsKey(lb_index(x_i))) {
                layers.getLast().put(lb_index(x_i), new Bound(lb(x_i), assigns.get(lb_index(x_i)).reason));
            }
            assigns.set(lb_index(x_i), new Bound(val, new Lit(p.v, p.sign)));

            if (vals.get(x_i).lt(val) && tableau.containsKey(x_i)) {
                update(x_i, val);
            }

            // unate propagation..
            for (Assertion c : a_watches.get(x_i)) {
                if (!c.propagate_lb(x_i, cnfl)) {
                    return false;
                }
            }
            // bound propagation..
            for (Row c : t_watches.get(x_i)) {
                if (!c.propagate_lb(x_i, cnfl)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean assert_upper(final int x_i, final InfRational val, final Lit p, final Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        if (val.geq(ub(x_i))) {
            return true;
        } else if (val.lt(lb(x_i))) {
            cnfl.add(p.not());                                 // either the literal 'p' is false ..
            cnfl.add(assigns.get(lb_index(x_i)).reason.not()); // or what asserted the lower bound is false..
            return false;
        } else {
            if (!layers.isEmpty() && !layers.getLast().containsKey(ub_index(x_i))) {
                layers.getLast().put(ub_index(x_i), new Bound(ub(x_i), assigns.get(ub_index(x_i)).reason));
            }
            assigns.set(ub_index(x_i), new Bound(val, new Lit(p.v, p.sign)));

            if (vals.get(x_i).gt(val) && tableau.containsKey(x_i)) {
                update(x_i, val);
            }

            // unate propagation..
            for (Assertion c : a_watches.get(x_i)) {
                if (!c.propagate_ub(x_i, cnfl)) {
                    return false;
                }
            }
            // bound propagation..
            for (Row c : t_watches.get(x_i)) {
                if (!c.propagate_ub(x_i, cnfl)) {
                    return false;
                }
            }

            return true;
        }
    }

    private void update(final int x_i, final InfRational v) {
        assert tableau.containsKey(x_i) : "x_i should be a non-basic variable..";
        for (Row row : t_watches.get(x_i)) {
            // x_j = x_j + a_ji(v - x_i)..
            vals.get(row.x).add(v.minus(vals.get(x_i)).times(row.l.vars.get(x_i)));
            Collection<LRAValueListener> ls = listeners.get(row.x);
            if (ls != null) {
                for (LRAValueListener l : ls) {
                    l.lraValueChange(row.x);
                }
            }
        }
        // x_i = v..
        vals.set(x_i, v);
        Collection<LRAValueListener> ls = listeners.get(x_i);
        if (ls != null) {
            for (LRAValueListener l : ls) {
                l.lraValueChange(x_i);
            }
        }
    }

    private void pivot_and_update(final int x_i, final int x_j, final InfRational v) {
        assert tableau.containsKey(x_i) : "x_i should be a basic variable..";
        assert !tableau.containsKey(x_j) : "x_j should be a non-basic variable..";
        assert tableau.get(x_i).l.vars.containsKey(x_j);

        final InfRational theta = v.minus(vals.get(x_i)).divide(tableau.get(x_i).l.vars.get(x_j));
        assert !theta.rat.isInfinite();

        // x_i = v
        vals.set(x_i, v);
        Collection<LRAValueListener> x_i_ls = listeners.get(x_i);
        if (x_i_ls != null) {
            for (LRAValueListener l : x_i_ls) {
                l.lraValueChange(x_i);
            }
        }

        // x_j += theta
        vals.get(x_j).add(theta);
        Collection<LRAValueListener> x_j_ls = listeners.get(x_j);
        if (x_j_ls != null) {
            for (LRAValueListener l : x_j_ls) {
                l.lraValueChange(x_j);
            }
        }
        for (Row row : t_watches.get(x_j)) {
            if (row.x != x_i) {
                // x_k += a_kj * theta..
                vals.get(row.x).add(theta.times(row.l.vars.get(x_j)));
                Collection<LRAValueListener> x_k_ls = listeners.get(x_j);
                if (x_k_ls != null) {
                    for (LRAValueListener l : x_k_ls) {
                        l.lraValueChange(row.x);
                    }
                }
            }
        }

        pivot(x_i, x_j);
    }

    private void pivot(final int x_i, final int x_j) {
        // the exiting row..
        Row row = tableau.remove(x_i);
        t_watches.removeAll(row.l.vars.keySet());

        final Rational c = row.l.vars.remove(x_j);
        row.l.divide(c.minus());
        row.l.vars.put(x_i, ONE.divide(c));

        for (Row r : new ArrayList<>(t_watches.get(x_j))) {
            for (Integer v : r.l.vars.keySet()) {
                t_watches.get(v).remove(r);
            }
            r.l.add(row.l.times(r.l.vars.remove(x_j)));
            for (Integer v : r.l.vars.keySet()) {
                t_watches.get(v).add(r);
            }
        }

        // we add a new row into the tableau..
        tableau.put(x_j, new Row(this, x_j, row.l));
    }

    static int lb_index(final int v) {
        return v << 1;
    }

    static int ub_index(final int v) {
        return (v << 1) ^ 1;
    }

    static class Bound {

        final InfRational value; // the value of the bound..
        final Lit reason; // the reason for the value..

        private Bound(InfRational value, Lit reason) {
            this.value = value;
            this.reason = reason;
        }
    }
}
