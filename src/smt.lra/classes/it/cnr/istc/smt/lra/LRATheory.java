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
import static it.cnr.istc.common.Rational.NEGATIVE_INFINITY;
import static it.cnr.istc.common.Rational.POSITIVE_INFINITY;
import static it.cnr.istc.common.Rational.ZERO;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import it.cnr.istc.smt.Theory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class LRATheory implements Theory {

    private final SatCore sat_core;
    private final List<Bound> assigns = new ArrayList<>(); // the current assignments..
    private final List<InfRational> vals = new ArrayList<>(); // the current values..
    private final Map<Integer, Row> tableau = new TreeMap<>(); // the sparse matrix..
    private final Map<String, Integer> exprs = new HashMap<>(); // the expressions (string to numeric variable) for which already exist slack variables..
    private final Map<String, Integer> s_asrts = new HashMap<>(); // the assertions (string to boolean variable) used for reducing the number of boolean variables..
    private final Map<Integer, Assertion> v_asrts = new HashMap<>(); // the assertions (boolean variable to assertion) used for enforcing (negating) assertions..
    private final List<Collection<Assertion>> a_watches = new ArrayList<>(); // for each variable 'v', a list of assertions watching 'v'..
    private final List<Set<Row>> t_watches = new ArrayList<>(); // for each variable 'v', a list of tableau rows watching 'v'..
    private final Deque<Map<Integer, Bound>> layers = new ArrayDeque<>(); // we store the updated bounds..
    private final Map<Integer, Collection<LRAValueListener>> listeners = new HashMap<>();

    public LRATheory(final SatCore core) {
        this.sat_core = core;
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

    private static class Bound {

        private final InfRational value; // the value of the bound..
        private final Lit reason; // the reason for the value..

        private Bound(InfRational value, Lit reason) {
            this.value = value;
            this.reason = reason;
        }
    }
}
