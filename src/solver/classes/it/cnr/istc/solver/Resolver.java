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

import it.cnr.istc.common.Lin;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Resolver {

    protected final Solver slv; // the solver this resolver belongs to..
    final int rho; // the propositional variable indicating whether the resolver is active or not..
    final Lin cost; // the intrinsic cost of the resolver..
    final Collection<Flaw> preconditions = new ArrayList<>(); // the preconditions of this resolver..
    final Flaw effect;  // the flaw solved by this resolver..
    double est_cost = Double.POSITIVE_INFINITY; // the estimated cost of the resolver..

    public Resolver(Solver slv, int rho, Lin cost, Flaw effect) {
        this.slv = slv;
        this.rho = rho;
        this.cost = cost;
        this.effect = effect;
    }
}
