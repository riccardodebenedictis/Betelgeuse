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
package it.cnr.istc.core;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Atom extends Item {

    public final int sigma; // this variable represents the state of the atom: if the variable is true, the atom is active; if the variable is false, the atom is unified; if the variable is undefined, the atom is not justified..

    public Atom(final Core core, final IEnv env, final Predicate p) {
        super(core, env, p);
        this.sigma = core.sat_core.newVar();
    }

    @Override
    public String toString() {
        switch (core.sat_core.value(sigma)) {
            case False:
                return "Unified " + super.toString();
            case True:
                return "Active " + super.toString();
            case Undefined:
                return super.toString();
            default:
                throw new AssertionError(core.sat_core.value(sigma).name());
        }
    }
}
