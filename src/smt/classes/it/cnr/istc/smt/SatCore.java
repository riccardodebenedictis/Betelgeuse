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
package it.cnr.istc.smt;

import static it.cnr.istc.smt.LBool.False;
import static it.cnr.istc.smt.LBool.True;
import static it.cnr.istc.smt.LBool.Undefined;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class SatCore {

    public static final int FALSE_var = 0;
    public static final int TRUE_var = 1;
    final List<Clause> clauses = new ArrayList<>(); // collection of problem constraints..
    final List<List<Clause>> watches = new ArrayList<>(); // for each literal 'p', a list of constraints watching 'p'..
    final List<Lit> prop_q = new LinkedList<>(); // propagation queue..
    final List<LBool> assigns = new ArrayList<>(); // the current assignments..
    final List<Lit> trail = new LinkedList<>(); // the list of assignment in chronological order..
    final List<Integer> trail_lim = new ArrayList<>(); // separator indices for different decision levels in 'trail'..
    final List<Clause> reason = new ArrayList<>(); // for each variable, the constraint that implied its value..
    final List<Integer> level = new ArrayList<>(); // for each variable, the decision level it was assigned..
    final Map<String, Integer> exprs = new HashMap<>(); // the already existing expressions (string to bool variable)..
    private final Map<Integer, Collection<SatListener>> listeners = new HashMap<>();

    public SatCore() {
        int c_false = newVar();
        int c_true = newVar();
        assert c_false == FALSE_var;
        assert c_true == TRUE_var;
        assigns.set(FALSE_var, False);
        assigns.set(TRUE_var, True);
    }

    public int newVar() {
        final int id = assigns.size();
        assigns.add(Undefined);
        watches.add(new ArrayList<>());
        watches.add(new ArrayList<>());
        exprs.put("b" + id, id);
        level.add(0);
        reason.add(null);
        return id;
    }

    public boolean newClause(final Lit... lits) {
        final List<Lit> c_lits = new ArrayList<>();
        for (Lit lit : lits) {
            switch (value(lit)) {
                case True:
                    return true; // the clause is already satisfied..
                case Undefined:
                    boolean found = false;
                    for (Lit c_lit : c_lits) {
                        if (c_lit.v == lit.v) {
                            if (c_lit.sign == lit.sign) {
                                found = true;
                                break;
                            } else {
                                return true; // the clause represents a tautology..
                            }
                        }
                    }
                    if (!found) {
                        c_lits.add(lit);
                    }
                    break;
            }
        }
        switch (c_lits.size()) {
            case 0:
                return false;
            case 1:
                return enqueue(c_lits.get(0), null);
            default:
                clauses.add(new Clause(this, c_lits.toArray(new Lit[c_lits.size()])));
                return true;
        }
    }

    public LBool value(final int x) {
        return assigns.get(x);
    }

    public LBool value(final Lit p) {
        switch (value(p.v)) {
            case False:
                return p.sign ? False : True;
            case True:
                return p.sign ? True : False;
            case Undefined:
                return Undefined;
            default:
                throw new AssertionError(value(p.v).name());
        }
    }

    public int index(final Lit p) {
        return p.sign ? p.v << 1 : (p.v << 1) ^ 1;
    }

    /**
     * Returns the current decision level.
     *
     * @return the current decision level.
     */
    public int decisionLevel() {
        return trail_lim.size();
    }

    /**
     * Checks whether this decision level is root level.
     *
     * @return whether this decision level is root level.
     */
    public boolean rootLevel() {
        return trail_lim.isEmpty();
    }

    public void listen(final int v, final SatListener l) {
        Collection<SatListener> ls = listeners.get(v);
        if (ls == null) {
            ls = new ArrayList<>();
            listeners.put(v, ls);
        }
        ls.add(l);
    }

    private boolean enqueue(final Lit p, final Clause c) {
        switch (value(p)) {
            case False:
                return false;
            case True:
                return true;
            case Undefined:
                assigns.set(p.v, p.sign ? True : False);
                level.set(p.v, decisionLevel());
                reason.set(p.v, c);
                trail.add(p);
                prop_q.add(p);
                Collection<SatListener> ls = listeners.get(p.v);
                if (ls != null) {
                    for (SatListener l : ls) {
                        l.satValueChange(p.v);
                    }
                }
                return true;
            default:
                throw new AssertionError(value(p).name());
        }
    }
}
