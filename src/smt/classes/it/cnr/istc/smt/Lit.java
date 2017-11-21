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

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Lit {

    public final int v;
    public final boolean sign;

    public Lit(int v) {
        this(v, true);
    }

    public Lit(int v, boolean sign) {
        this.v = v;
        this.sign = sign;
    }

    public Lit not() {
        return new Lit(v, !sign);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.v;
        hash = 97 * hash + (this.sign ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Lit other = (Lit) obj;
        return this.v == other.v && this.sign == other.sign;
    }

    @Override
    public String toString() {
        return (sign ? "" : "¬") + "b" + v;
    }
}
