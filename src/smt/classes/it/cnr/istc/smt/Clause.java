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
import static it.cnr.istc.smt.SatCore.index;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Clause {

    private final SatCore core;
    Lit[] lits;

    public Clause(final SatCore core, final Lit[] lits) {
        assert lits.length >= 2;
        this.core = core;
        this.lits = lits;
        core.watches.get(index(lits[0].not())).add(this);
        core.watches.get(index(lits[1].not())).add(this);
    }

    public boolean propagate(final Lit p) {
        // make sure false literal is lits[1]..
        if (lits[0].v == p.v) {
            Lit tmp = lits[0];
            lits[0] = lits[1];
            lits[1] = tmp;
        }

        // if 0th watch is true, the clause is already satisfied..
        if (core.value(lits[0]) == True) {
            core.watches.get(core.index(p)).add(this);
            return true;
        }

        // we look for a new literal to watch..
        for (int i = 1; i < lits.length; i++) {
            if (core.value(lits[i]) != False) {
                Lit tmp = lits[1];
                lits[1] = lits[i];
                lits[i] = tmp;
                core.watches.get(core.index(lits[1].not())).add(this);
                return true;
            }
        }

        // clause is unit under assignment..
        core.watches.get(core.index(p)).add(this);
        return core.enqueue(lits[0], this);
    }

    @Override
    public String toString() {
        return Stream.of(lits).map(l -> l.toString()).collect(Collectors.joining(", "));
    }
}
