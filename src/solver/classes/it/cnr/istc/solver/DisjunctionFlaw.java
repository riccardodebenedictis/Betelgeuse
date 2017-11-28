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
package it.cnr.istc.solver;

import it.cnr.istc.core.Disjunction;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class DisjunctionFlaw extends Flaw {

    private final Disjunction dsj;

    DisjunctionFlaw(final Solver slv, final Resolver cause, final Disjunction dsj) {
        super(slv, cause == null ? Collections.emptyList() : Arrays.asList(cause), true, true);
        this.dsj = dsj;
    }

    @Override
    void compute_resolvers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
