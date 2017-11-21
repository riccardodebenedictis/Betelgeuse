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

import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import it.cnr.istc.smt.Theory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class VarTheory implements Theory {

    private final SatCore sat_core;
    private final List<Map<IVarVal, Integer>> assigns = new ArrayList<>(); // the current assignments (val to bool variable)..
    private final Map<String, Integer> exprs = new HashMap<>(); // the already existing expressions (string to bool variable)..
    private final Map<Integer, Collection<Integer>> is_contained_in = new HashMap<>(); // the boolean variable contained in the set variables (bool variable to vector of set variables)..
    private final Deque<Set<Integer>> layers = new ArrayDeque<>(); // we store the updated variables..
    private final Map<Integer, Collection<IVarListener>> listeners = new HashMap<>();

    public VarTheory(final SatCore core) {
        this.sat_core = core;
    }

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
}
