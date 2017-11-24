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
import it.cnr.istc.smt.Lit;
import java.util.Collection;

/**
 * This class is used for representing assertions of the linear real arithmetic
 * (LRA) theory.
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class Assertion {

    private final LRATheory th;
    final Op op;
    final int b; // the controlling variable..
    final int x; // the constrained (numeric) variable..
    final InfRational v; // the value..

    Assertion(final LRATheory th, final Op op, final int b, final int x, final InfRational v) {
        this.th = th;
        this.op = op;
        this.b = b;
        this.x = x;
        this.v = v;
        th.a_watches.get(x).add(this);
    }

    /**
     * Notifies, for propagation purposes, this assertion that the lower bound
     * of variable {@code x_i} has changed.
     *
     * @param x_i the variable whose lower bound has changed.
     * @param cnfl the conflict clause in case propagation fails.
     * @return {@code true} if propagation succeeds.
     */
    boolean propagate_lb(final int x_i, final Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        if (th.lb(x_i).gt(v)) {
            switch (op) {
                case LEq: // the assertion is unsatisfable: [x_i >= lb(x_i)] -> ![x_i <= v]..
                    switch (th.sat_core.value(b)) {
                        case True:                                                          // we have a propositional inconsistency..
                            cnfl.add(new Lit(b, false));                                    // either the literal 'b' is false ..
                            cnfl.add(th.assigns.get(LRATheory.lb_index(x_i)).reason.not()); // or what asserted the lower bound is false..
                            return false;
                        case Undefined: // we propagate information to the sat core..
                            th.sat_core.record(new Lit(b, false), th.assigns.get(LRATheory.lb_index(x_i)).reason.not());
                    }
                    break;
                case GEq: // the assertion is satisfied; [x_i >= lb(x_i)] -> [x_i >= v]..
                    switch (th.sat_core.value(b)) {
                        case False:                                                         // we have a propositional inconsistency..
                            cnfl.add(new Lit(b));                                           // either the literal 'b' is true ..
                            cnfl.add(th.assigns.get(LRATheory.lb_index(x_i)).reason.not()); // or what asserted the lower bound is false..
                            return false;
                        case Undefined: // we propagate information to the sat core..
                            th.sat_core.record(new Lit(b), th.assigns.get(LRATheory.lb_index(x_i)).reason.not());
                    }
                    break;
            }
        }

        return true;
    }

    /**
     * Notifies, for propagation purposes, this assertion that the upper bound
     * of variable {@code x_i} has changed.
     *
     * @param x_i the variable whose upper bound has changed.
     * @param cnfl the conflict clause in case propagation fails.
     * @return {@code true} if propagation succeeds.
     */
    boolean propagate_ub(final int x_i, final Collection<Lit> cnfl) {
        assert cnfl.isEmpty();
        if (th.ub(x_i).lt(v)) {
            switch (op) {
                case LEq: // the assertion is satisfied: [x_i <= ub(x_i)] -> [x_i <= v]..
                    switch (th.sat_core.value(b)) {
                        case False:                                                         // we have a propositional inconsistency..
                            cnfl.add(new Lit(b));                                           // either the literal 'b' is true ..
                            cnfl.add(th.assigns.get(LRATheory.ub_index(x_i)).reason.not()); // or what asserted the upper bound is false..
                            return false;
                        case Undefined: // we propagate information to the sat core..
                            th.sat_core.record(new Lit(b), th.assigns.get(LRATheory.ub_index(x_i)).reason.not());
                    }
                    break;
                case GEq: // the assertion is unsatisfable; [x_i <= ub(x_i)] -> ![x_i >= v]..
                    switch (th.sat_core.value(b)) {
                        case True:                                                          // we have a propositional inconsistency..
                            cnfl.add(new Lit(b, false));                                    // either the literal 'b' is false ..
                            cnfl.add(th.assigns.get(LRATheory.ub_index(x_i)).reason.not()); // or what asserted the upper bound is false..
                            return false;
                        case Undefined: // we propagate information to the sat core..
                            th.sat_core.record(new Lit(b, false), th.assigns.get(LRATheory.ub_index(x_i)).reason.not());
                    }
                    break;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[b").append(b).append("] ");
        sb.append("x").append(x);
        switch (op) {
            case LEq:
                sb.append(" <= ");
                break;
            case GEq:
                sb.append(" >= ");
                break;
            default:
                throw new AssertionError(op.name());
        }
        sb.append(v.toString());
        return sb.toString();
    }

    enum Op {
        LEq, GEq
    }
}
