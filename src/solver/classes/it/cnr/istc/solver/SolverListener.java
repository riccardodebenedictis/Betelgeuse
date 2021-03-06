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

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public interface SolverListener {

    default void newFlaw(final Flaw f) {
        f.slv.sat_core.listen(f.getPhi(), (int v) -> flawStateChanged(f));
        flawCreated(f);
    }

    void flawCreated(final Flaw f);

    void flawStateChanged(final Flaw f);

    void currentFlaw(final Flaw f);

    default void newResolver(final Resolver r) {
        r.slv.sat_core.listen(r.rho, (int v) -> resolverStateChanged(r));
        resolverCreated(r);
    }

    void resolverCreated(final Resolver r);

    void resolverStateChanged(final Resolver r);

    void resolverCostChanged(final Resolver r);

    void currentResolver(final Resolver r);

    void newCausalLink(final Flaw f, final Resolver r);
}
