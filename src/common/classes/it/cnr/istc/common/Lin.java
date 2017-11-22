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
package it.cnr.istc.common;

import static it.cnr.istc.common.Rational.ONE;
import static it.cnr.istc.common.Rational.ZERO;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Lin {

    private final Map<Integer, Rational> vars = new TreeMap<>();
    private Rational known_term;

    public Lin() {
        this.known_term = new Rational();
    }

    public Lin(final int v) {
        vars.put(v, new Rational(ONE));
        this.known_term = new Rational();
    }

    public Lin(final Rational known_term) {
        this.known_term = known_term;
    }

    public Lin(final int v, final Rational c) {
        vars.put(v, c);
        this.known_term = new Rational();
    }

    public void add(final int v, final Rational c) {
        Rational rat = vars.get(v);
        if (rat != null) {
            rat.add(c);
            if (rat.eq(ZERO)) {
                vars.remove(v);
            }
        } else {
            vars.put(v, new Rational(c));
        }
    }

    public void add(final Lin rhs) {
        for (Map.Entry<Integer, Rational> entry : rhs.vars.entrySet()) {
            Rational rat = vars.get(entry.getKey());
            if (rat != null) {
                rat.add(entry.getValue());
            } else {
                vars.put(entry.getKey(), new Rational(entry.getValue()));
            }
        }
        vars.entrySet().removeIf(term -> term.getValue().eq(ZERO));
        known_term.add(rhs.known_term);
    }

    public void sub(final Lin rhs) {
        for (Map.Entry<Integer, Rational> entry : rhs.vars.entrySet()) {
            Rational rat = vars.get(entry.getKey());
            if (rat != null) {
                rat.sub(entry.getValue());
            } else {
                vars.put(entry.getKey(), entry.getValue().minus());
            }
        }
        vars.entrySet().removeIf(term -> term.getValue().eq(ZERO));
        known_term.sub(rhs.known_term);
    }

    public void add(final Rational rhs) {
        known_term.add(rhs);
    }

    public void sub(final Rational rhs) {
        known_term.sub(rhs);
    }

    public void mult(final Rational rhs) {
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            vars.get(entry.getKey()).mult(rhs);
        }
        vars.entrySet().removeIf(term -> term.getValue().eq(ZERO));
        known_term.mult(rhs);
    }

    public void div(final Rational rhs) {
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            vars.get(entry.getKey()).div(rhs);
        }
        vars.entrySet().removeIf(term -> term.getValue().eq(ZERO));
        known_term.div(rhs);
    }

    public void add(final long rhs) {
        known_term.add(rhs);
    }

    public void sub(final long rhs) {
        known_term.sub(rhs);
    }

    public void mult(final long rhs) {
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            vars.get(entry.getKey()).mult(rhs);
        }
        vars.entrySet().removeIf(term -> term.getValue().eq(ZERO));
        known_term.mult(rhs);
    }

    public void div(final long rhs) {
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            vars.get(entry.getKey()).div(rhs);
        }
        vars.entrySet().removeIf(term -> term.getValue().eq(ZERO));
        known_term.div(rhs);
    }

    public Lin plus(final Lin rhs) {
        Lin lin = new Lin(known_term.plus(rhs.known_term));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), new Rational(entry.getValue()));
        }
        for (Map.Entry<Integer, Rational> entry : rhs.vars.entrySet()) {
            Rational rat = lin.vars.get(entry.getKey());
            if (rat != null) {
                rat.add(entry.getKey());
            } else {
                lin.vars.put(entry.getKey(), new Rational(entry.getValue()));
            }
        }
        return lin;
    }

    public Lin minus(final Lin rhs) {
        Lin lin = new Lin(known_term.minus(rhs.known_term));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), new Rational(entry.getValue()));
        }
        for (Map.Entry<Integer, Rational> entry : rhs.vars.entrySet()) {
            Rational rat = lin.vars.get(entry.getKey());
            if (rat != null) {
                rat.sub(entry.getKey());
            } else {
                lin.vars.put(entry.getKey(), entry.getValue().minus());
            }
        }
        return lin;
    }

    public Lin plus(final Rational rhs) {
        Lin lin = new Lin(known_term.plus(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), new Rational(entry.getValue()));
        }
        return lin;
    }

    public Lin minus(final Rational rhs) {
        Lin lin = new Lin(known_term.minus(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), new Rational(entry.getValue()));
        }
        return lin;
    }

    public Lin times(final Rational rhs) {
        Lin lin = new Lin(known_term.times(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().times(rhs));
        }
        return lin;
    }

    public Lin divide(final Rational rhs) {
        Lin lin = new Lin(known_term.divide(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().divide(rhs));
        }
        return lin;
    }

    public Lin plus(final long rhs) {
        Lin lin = new Lin(known_term.plus(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().divide(rhs));
        }
        return lin;
    }

    public Lin minus(final long rhs) {
        Lin lin = new Lin(known_term.minus(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().divide(rhs));
        }
        return lin;
    }

    public Lin times(final long rhs) {
        Lin lin = new Lin(known_term.times(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().divide(rhs));
        }
        return lin;
    }

    public Lin divide(final long rhs) {
        Lin lin = new Lin(known_term.divide(rhs));
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().divide(rhs));
        }
        return lin;
    }

    public Lin minus() {
        Lin lin = new Lin(known_term.minus());
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            lin.vars.put(entry.getKey(), entry.getValue().minus());
        }
        return lin;
    }

    @Override
    public String toString() {
        if (vars.isEmpty()) {
            return known_term.toString();
        }

        StringBuilder str = new StringBuilder();
        Map.Entry<Integer, Rational> first = vars.entrySet().iterator().next();
        for (Map.Entry<Integer, Rational> entry : vars.entrySet()) {
            if (entry == first) {
                if (entry.getValue().eq(ONE)) {
                    str.append("x");
                } else if (entry.getValue().eq(ONE.minus())) {
                    str.append("-x");
                } else {
                    str.append(entry.getValue().toString()).append("*x");
                }
                str.append(entry.getKey());
            } else {
                if (entry.getValue().eq(ONE)) {
                    str.append(" + x");
                } else if (entry.getValue().eq(ONE.minus())) {
                    str.append(" - x");
                } else if (entry.getValue().isPositive()) {
                    str.append(" + ").append(entry.getValue().toString()).append("*x");
                } else {
                    str.append(" - ").append(entry.getValue().toString()).append("*x");
                }
                str.append(entry.getKey());
            }
        }
        if (known_term.isPositive()) {
            str.append(" + ").append(known_term.toString());
        }
        if (known_term.isNegative()) {
            str.append(" - ").append(known_term.toString());
        }
        return str.toString();
    }
}
