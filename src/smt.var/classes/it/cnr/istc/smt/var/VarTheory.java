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
package it.cnr.istc.smt.var;

import static it.cnr.istc.smt.LBool.False;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import static it.cnr.istc.smt.SatCore.FALSE_var;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import it.cnr.istc.smt.Theory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class VarTheory implements Theory {

    private final SatCore sat_core;
    private final List<Map<IVarVal, Integer>> assigns = new ArrayList<>(); // the current assignments (val to bool variable)..
    private final Map<String, Integer> exprs = new HashMap<>(); // the already existing expressions (string to bool variable)..
    private final Map<Integer, Collection<Integer>> is_contained_in = new HashMap<>(); // the boolean variable contained in the object variables (bool variable to vector of object variables)..
    private final Deque<Set<Integer>> layers = new ArrayDeque<>(); // we store the updated variables..
    private final Map<Integer, Collection<VarValueListener>> listeners = new HashMap<>();

    public VarTheory(final SatCore core) {
        this.sat_core = core;
        core.addTheory(this);
    }

    /**
     * Creates and returns a new object variable having the {@code vals} allowed
     * values.
     *
     * @param vals the allowed values of the new object variable.
     * @return an integer representing the new object variable.
     */
    public int newVar(final Set<? extends IVarVal> vals) {
        assert !vals.isEmpty();
        final int id = assigns.size();
        assigns.add(new IdentityHashMap<>());
        if (vals.size() == 1) {
            assigns.get(id).put(vals.iterator().next(), TRUE_var);
        } else {
            for (IVarVal val : vals) {
                int bv = sat_core.newVar();
                assigns.get(id).put(val, bv);
                sat_core.bind(bv, this);
                Collection<Integer> ici = is_contained_in.get(bv);
                if (ici == null) {
                    ici = new ArrayList<>();
                    is_contained_in.put(bv, ici);
                }
                ici.add(id);
            }
        }
        return id;
    }

    /**
     * Creates and returns a new object variable having {@code vars} as
     * controlling variables and {@code vals} as allowed values.
     *
     * @param vars an array of integers representing the boolean variables
     * controlling value of the new object variable.
     * @param vals the allowed values of the new object variable.
     * @return an integer representing the new object variable.
     */
    public int newVar(final int[] vars, final IVarVal[] vals) {
        assert vars.length == vals.length;
        assert vars.length > 0;
        assert Arrays.stream(vars).allMatch(v -> is_contained_in.containsKey(v));
        final int id = assigns.size();
        assigns.add(new IdentityHashMap<>());
        for (int i = 0; i < vars.length; i++) {
            assigns.get(id).put(vals[i], vars[i]);
            is_contained_in.get(vars[i]).add(id);
        }
        return id;
    }

    /**
     * Returns the boolean variable controlling whether the {@code var} variable
     * can assume the {@code val} value.
     *
     * @param var an integer representing an object variable.
     * @param val a possible value for variable {@code var}.
     * @return an integer representing the boolean variable controlling whether
     * the {@code var} variable can assume the {@code val} value.
     */
    public int allows(final int var, final IVarVal val) {
        Integer b_var = assigns.get(var).get(val);
        if (b_var != null) {
            return b_var;
        } else {
            return FALSE_var;
        }
    }

    public int newEq(final int l, final int r) {
        if (l == r) {
            return TRUE_var;
        }
        if (l > r) {
            return newEq(r, l);
        }

        String s_expr = "e" + l + " == e" + r;
        Integer b_var = exprs.get(s_expr);
        if (b_var != null) {
            return b_var;
        } else {
            Set<IVarVal> intersection = new HashSet<>();
            Set<IVarVal> l_vals = value(l);
            Set<IVarVal> r_vals = value(r);
            if (l_vals.size() < r_vals.size()) {
                for (IVarVal l_val : l_vals) {
                    if (r_vals.contains(l_val)) {
                        intersection.add(l_val);
                    }
                }
            } else {
                for (IVarVal r_val : r_vals) {
                    if (l_vals.contains(r_val)) {
                        intersection.add(r_val);
                    }
                }
            }
            if (intersection.isEmpty()) {
                return FALSE_var;
            }

            // we need to create a new variable..
            b_var = sat_core.newVar();
            boolean nc;
            for (IVarVal l_val : l_vals) {
                if (!intersection.contains(l_val)) {
                    nc = sat_core.newClause(new Lit(b_var, false), new Lit(assigns.get(l).get(l_val), false));
                    assert nc;
                }
            }
            for (IVarVal r_val : r_vals) {
                if (!intersection.contains(r_val)) {
                    nc = sat_core.newClause(new Lit(b_var, false), new Lit(assigns.get(r).get(r_val), false));
                    assert nc;
                }
            }
            for (IVarVal val : intersection) {
                nc = sat_core.newClause(new Lit(b_var, false), new Lit(assigns.get(l).get(val), false), new Lit(assigns.get(r).get(val)));
                assert nc;
                nc = sat_core.newClause(new Lit(b_var, false), new Lit(assigns.get(l).get(val)), new Lit(assigns.get(r).get(val), false));
                assert nc;
                nc = sat_core.newClause(new Lit(b_var), new Lit(assigns.get(l).get(val), false), new Lit(assigns.get(r).get(val), false));
                assert nc;
            }
            exprs.put(s_expr, b_var);
            return b_var;
        }
    }

    public final Set<IVarVal> value(final int var) {
        return assigns.get(var).entrySet().stream().filter(entry -> sat_core.value(entry.getValue()) != False).map(entry -> entry.getKey()).collect(Collectors.toSet());
    }

    @Override
    public boolean propagate(Lit p, Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        for (Integer v : is_contained_in.get(p.v)) {
            Collection<VarValueListener> ls = listeners.get(v);
            if (ls != null) {
                for (VarValueListener l : ls) {
                    l.varValueChange(v);
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
        layers.addFirst(new HashSet<>());
    }

    @Override
    public void pop() {
        for (Integer v : layers.pollFirst()) {
            Collection<VarValueListener> ls = listeners.get(v);
            if (ls != null) {
                for (VarValueListener l : ls) {
                    l.varValueChange(v);
                }
            }
        }
    }

    public void listen(final int v, final VarValueListener l) {
        Collection<VarValueListener> ls = listeners.get(v);
        if (ls == null) {
            ls = new ArrayList<>();
            listeners.put(v, ls);
        }
        ls.add(l);
    }

    public void forget(final int v, final VarValueListener l) {
        Collection<VarValueListener> ls = listeners.get(v);
        ls.remove(l);
        if (ls.isEmpty()) {
            listeners.remove(v);
        }
    }
}
