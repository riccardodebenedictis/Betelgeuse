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

    public Lin(final Rational known_term) {
        this.known_term = known_term;
    }

    public Lin(final int v, final Rational c) {
        vars.put(v, c);
        this.known_term = new Rational();
    }
}
