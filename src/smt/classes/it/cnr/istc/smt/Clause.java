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
public class Clause {

    private final SatCore core;
    private Lit[] lits;

    public Clause(final SatCore core, final Lit[] lits) {
        assert lits.length >= 2;
        this.core = core;
        this.lits = lits;
        core.watches.get(core.index(lits[0].not())).add(this);
        core.watches.get(core.index(lits[1].not())).add(this);
    }

    public boolean propagate(final Lit p) {
        throw new UnsupportedOperationException("not supported yet..");
    }
}
