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

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class InfRational {

    private Rational rat; // the rational part..
    private Rational inf; // the infinitesimal part..

    public InfRational() {
        this.rat = new Rational();
    }

    public InfRational(final long num) {
        this.rat = new Rational(num);
    }

    public InfRational(final Rational rat) {
        this.rat = rat;
    }

    public InfRational(final long num, final long den) {
        this.rat = new Rational(num, den);
    }

    public InfRational(final Rational rat, final long inf) {
        this.rat = rat;
        this.inf = new Rational(inf);
    }

    public InfRational(final Rational rat, final Rational inf) {
        this.rat = rat;
        this.inf = inf;
    }

    public Rational getRational() {
        return rat;
    }

    public Rational getInfinitesimal() {
        return inf;
    }

    public boolean neq(final InfRational rhs) {
        return rat.neq(rhs.rat) && inf.neq(rhs.inf);
    }

    public boolean lt(final InfRational rhs) {
        return rat.lt(rhs.rat) || (rat.eq(rhs.rat) && inf.lt(rhs.inf));
    }

    public boolean leq(final InfRational rhs) {
        return rat.leq(rhs.rat) || (rat.eq(rhs.rat) && inf.leq(rhs.inf));
    }

    public boolean eq(final InfRational rhs) {
        return rat.eq(rhs.rat) && inf.eq(rhs.inf);
    }

    public boolean geq(final InfRational rhs) {
        return rat.geq(rhs.rat) || (rat.eq(rhs.rat) && inf.geq(rhs.inf));
    }

    public boolean gt(final InfRational rhs) {
        return rat.gt(rhs.rat) || (rat.eq(rhs.rat) && inf.gt(rhs.inf));
    }

    public boolean neq(final Rational rhs) {
        return rat != rhs || inf.numerator() != 0;
    }

    public boolean lt(final Rational rhs) {
        return rat.lt(rhs) || (rat.eq(rhs) && inf.numerator() < 0);
    }

    public boolean leq(final Rational rhs) {
        return rat.leq(rhs) || (rat.eq(rhs) && inf.numerator() <= 0);
    }

    public boolean eq(final Rational rhs) {
        return rat.eq(rhs) && inf.numerator() == 0;
    }

    public boolean geq(final Rational rhs) {
        return rat.geq(rhs) || (rat.eq(rhs) && inf.numerator() >= 0);
    }

    public boolean gt(final Rational rhs) {
        return rat.gt(rhs) || (rat.eq(rhs) && inf.numerator() > 0);
    }

    public boolean neq(final long rhs) {
        return rat.neq(rhs) || inf.numerator() != 0;
    }

    public boolean lt(final long rhs) {
        return rat.lt(rhs) || (rat.eq(rhs) && inf.numerator() < 0);
    }

    public boolean leq(final long rhs) {
        return rat.leq(rhs) || (rat.eq(rhs) && inf.numerator() <= 0);
    }

    public boolean eq(final long rhs) {
        return rat.eq(rhs) && inf.numerator() == 0;
    }

    public boolean geq(final long rhs) {
        return rat.geq(rhs) || (rat.eq(rhs) && inf.numerator() >= 0);
    }

    public boolean gt(final long rhs) {
        return rat.gt(rhs) || (rat.eq(rhs) && inf.numerator() > 0);
    }

    public InfRational plus(final InfRational rhs) {
        return new InfRational(rat.plus(rhs.rat), inf.plus(rhs.inf));
    }

    public InfRational minus(final InfRational rhs) {
        return new InfRational(rat.minus(rhs.rat), inf.minus(rhs.inf));
    }

    public InfRational plus(final Rational rhs) {
        return new InfRational(rat.plus(rhs), new Rational(inf));
    }

    public InfRational minus(final Rational rhs) {
        return new InfRational(rat.minus(rhs), new Rational(inf));
    }

    public InfRational times(final Rational rhs) {
        return new InfRational(rat.times(rhs), inf.times(rhs));
    }

    public InfRational divide(final Rational rhs) {
        return new InfRational(rat.divide(rhs), inf.divide(rhs));
    }

    public InfRational plus(final long rhs) {
        return new InfRational(rat.plus(rhs), new Rational(inf));
    }

    public InfRational minus(final long rhs) {
        return new InfRational(rat.minus(rhs), new Rational(inf));
    }

    public InfRational times(final long rhs) {
        return new InfRational(rat.times(rhs), inf.times(rhs));
    }

    public InfRational divide(final long rhs) {
        return new InfRational(rat.divide(rhs), inf.divide(rhs));
    }

    public void add(final InfRational rhs) {
        rat.add(rhs.rat);
        inf.add(rhs.inf);
    }

    public void sub(final InfRational rhs) {
        rat.sub(rhs.rat);
        inf.sub(rhs.inf);
    }

    public void add(final Rational rhs) {
        rat.add(rhs);
    }

    public void sub(final Rational rhs) {
        rat.sub(rhs);
    }

    public void mult(final Rational rhs) {
        rat.add(rhs);
    }

    public void div(final Rational rhs) {
        rat.sub(rhs);
    }

    public void add(final long rhs) {
        rat.add(rhs);
    }

    public void sub(final long rhs) {
        rat.sub(rhs);
    }

    public void mult(final long rhs) {
        rat.add(rhs);
    }

    public void div(final long rhs) {
        rat.sub(rhs);
    }

    public InfRational minus() {
        return new InfRational(rat.minus(), inf.minus());
    }

    @Override
    public String toString() {
        if (rat.isInfinite() || inf.eq(ZERO)) {
            return rat.toString();
        }
        String c_str = new String();
        if (rat.neq(ZERO)) {
            c_str += rat.toString();
        }
        if (inf.eq(ONE)) {
            if (c_str.isEmpty()) {
                c_str += "ε";
            } else {
                c_str += " + ε";
            }
        } else if (inf.eq(ONE.minus())) {
            if (c_str.isEmpty()) {
                c_str += "-ε";
            } else {
                c_str += " - ε";
            }
        } else if (inf.isNegative()) {
            if (c_str.isEmpty()) {
                c_str += inf.toString() + "ε";
            } else {
                c_str += " " + inf.toString() + "ε";
            }
        } else if (c_str.isEmpty()) {
            c_str += inf.toString() + "ε";
        } else {
            c_str += " +" + inf.toString() + "ε";
        }
        return c_str;
    }
}
