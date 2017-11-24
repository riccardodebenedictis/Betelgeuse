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
import it.cnr.istc.smt.Lit;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class is used for representing tableau rows.
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class Row {

    private final LRATheory th;
    final int x; // the basic variable..
    final Lin l; // the linear expression..

    Row(final LRATheory th, final int x, final Lin l) {
        this.th = th;
        this.x = x;
        this.l = l;
        for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
            th.t_watches.get(term.getKey()).add(this);
        }
    }

    /**
     * Notifies, for propagation purposes, this tableau row that the lower bound
     * of variable {@code x_i} has changed.
     *
     * @param x_i the variable whose lower bound has changed.
     * @param cnfl the conflict clause in case propagation fails.
     * @return {@code true} if propagation succeeds.
     */
    boolean propagate_lb(final int x_i, final Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        // we make room for the first literal..
        cnfl.add(new Lit(0));
        if (l.vars.get(x_i).isPositive()) {
            InfRational lb = new InfRational();
            for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
                if (term.getValue().isPositive()) {
                    if (th.lb(term.getKey()).rat.isNegativeInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        lb.add(th.lb(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.lb_index(term.getKey())).reason.not());
                    }
                } else if (term.getValue().isNegative()) {
                    if (th.ub(term.getKey()).rat.isPositiveInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        lb.add(th.ub(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.ub_index(term.getKey())).reason.not());
                    }
                }
            }

            if (lb.gt(th.lb(x))) {
                for (Assertion c : th.a_watches.get(x)) {
                    if (lb.gt(c.v)) {
                        switch (c.op) {
                            case LEq: // the assertion is unsatisfable..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b, false));
                                switch (th.sat_core.value(c.b)) {
                                    case True: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                            case GEq: // the assertion is satisfied..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b));
                                switch (th.sat_core.value(c.b)) {
                                    case False: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                        }
                    }
                }
            }
        } else {
            InfRational ub = new InfRational();
            for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
                if (term.getValue().isPositive()) {
                    if (th.ub(term.getKey()).rat.isPositiveInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        ub.add(th.ub(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.ub_index(term.getKey())).reason.not());
                    }
                } else if (term.getValue().isNegative()) {
                    if (th.lb(term.getKey()).rat.isNegativeInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        ub.add(th.lb(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.lb_index(term.getKey())).reason.not());
                    }
                }
            }

            if (ub.lt(th.ub(x))) {
                for (Assertion c : th.a_watches.get(x)) {
                    if (ub.lt(c.v)) {
                        switch (c.op) {
                            case LEq: // the assertion is satisfied..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b));
                                switch (th.sat_core.value(c.b)) {
                                    case False: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                            case GEq: // the assertion is unsatisfable..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b, false));
                                switch (th.sat_core.value(c.b)) {
                                    case True: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                        }
                    }
                }
            }
        }

        cnfl.clear();
        return true;
    }

    /**
     * Notifies, for propagation purposes, this tableau row that the upper bound
     * of variable {@code x_i} has changed.
     *
     * @param x_i the variable whose upper bound has changed.
     * @param cnfl the conflict clause in case propagation fails.
     * @return {@code true} if propagation succeeds.
     */
    boolean propagate_ub(final int x_i, final Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        // we make room for the first literal..
        cnfl.add(new Lit(0));
        if (l.vars.get(x_i).isPositive()) {
            InfRational ub = new InfRational();
            for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
                if (term.getValue().isPositive()) {
                    if (th.ub(term.getKey()).rat.isPositiveInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        ub.add(th.ub(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.ub_index(term.getKey())).reason.not());
                    }
                } else if (term.getValue().isNegative()) {
                    if (th.lb(term.getKey()).rat.isNegativeInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        ub.add(th.lb(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.lb_index(term.getKey())).reason.not());
                    }
                }
            }

            if (ub.lt(th.ub(x))) {
                for (Assertion c : th.a_watches.get(x)) {
                    if (ub.lt(c.v)) {
                        switch (c.op) {
                            case LEq: // the assertion is satisfied..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b));
                                switch (th.sat_core.value(c.b)) {
                                    case False: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                            case GEq: // the assertion is unsatisfable..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b, false));
                                switch (th.sat_core.value(c.b)) {
                                    case True: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                        }
                    }
                }
            }
        } else {
            InfRational lb = new InfRational();
            for (Map.Entry<Integer, Rational> term : l.vars.entrySet()) {
                if (term.getValue().isPositive()) {
                    if (th.lb(term.getKey()).rat.isNegativeInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        lb.add(th.lb(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.lb_index(term.getKey())).reason.not());
                    }
                } else if (term.getValue().isNegative()) {
                    if (th.ub(term.getKey()).rat.isPositiveInfinite()) {
                        // nothing to propagate..
                        cnfl.clear();
                        return true;
                    } else {
                        lb.add(th.ub(term.getKey()).times(term.getValue()));
                        cnfl.add(th.assigns.get(LRATheory.ub_index(term.getKey())).reason.not());
                    }
                }
            }

            if (lb.gt(th.lb(x))) {
                for (Assertion c : th.a_watches.get(x)) {
                    if (lb.gt(c.v)) {
                        switch (c.op) {
                            case LEq: // the assertion is unsatisfable..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b, false));
                                switch (th.sat_core.value(c.b)) {
                                    case True: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                            case GEq: // the assertion is satisfied..
                                ((List<Lit>) cnfl).set(0, new Lit(c.b));
                                switch (th.sat_core.value(c.b)) {
                                    case False: // we have a propositional inconsistency..
                                        return false;
                                    case Undefined: // we propagate information to the sat core..
                                        th.sat_core.record(cnfl.toArray(new Lit[cnfl.size()]));
                                }
                                break;
                        }
                    }
                }
            }
        }

        cnfl.clear();
        return true;
    }

    @Override
    public String toString() {
        return "x" + x + " == " + l.toString();
    }
}
