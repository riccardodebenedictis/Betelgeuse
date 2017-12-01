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

import it.cnr.istc.smt.lra.Rational;
import it.cnr.istc.core.CoreException;
import static it.cnr.istc.smt.LBool.Undefined;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public abstract class Resolver {

    protected final Solver slv; // the solver this resolver belongs to..
    public final int rho; // the propositional variable indicating whether the resolver is active or not..
    final Rational cost; // the intrinsic cost of the resolver..
    final Collection<Flaw> preconditions = new ArrayList<>(); // the preconditions of this resolver..
    final Flaw effect;  // the flaw solved by this resolver..
    Rational est_cost = new Rational(Rational.POSITIVE_INFINITY); // the estimated cost of the resolver..

    public Resolver(Solver slv, Rational cost, Flaw effect) {
        this(slv, slv.sat_core.newVar(), cost, effect);
    }

    public Resolver(Solver slv, int rho, Rational cost, Flaw effect) {
        this.slv = slv;
        this.rho = rho;
        this.cost = cost;
        this.effect = effect;
    }

    public Solver getSolver() {
        return slv;
    }

    public Flaw getEffect() {
        return effect;
    }

    void init() {
        if (slv.sat_core.value(rho) == Undefined) {
            // we do not have a top-level (a landmark) resolver..
            Collection<Resolver> ress = slv.rhos.get(rho);
            if (ress == null) {
                ress = new ArrayList<>();
                slv.rhos.put(rho, ress);
            }
            ress.add(this);
            slv.sat_core.bind(rho, slv);
        }
    }

    public Rational getEstimatedCost() {
        return cost.plus(est_cost);
    }

    protected abstract void expand() throws CoreException;

    public abstract String getLabel();
}
