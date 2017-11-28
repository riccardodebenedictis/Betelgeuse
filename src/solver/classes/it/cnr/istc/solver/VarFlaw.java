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

import it.cnr.istc.common.Rational;
import it.cnr.istc.core.Item.VarItem;
import it.cnr.istc.smt.var.IVarVal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class VarFlaw extends Flaw {

    private final VarItem var_item;

    public VarFlaw(final Solver slv, final Resolver cause, final VarItem var_item) {
        super(slv, cause == null ? Collections.emptyList() : Arrays.asList(cause), true, true);
        this.var_item = var_item;
    }

    @Override
    void compute_resolvers() {
        Set<IVarVal> vals = slv.var_theory.value(var_item.var);
        for (IVarVal val : vals) {
            add_resolver(new ChooseVal(slv, new Rational(1, vals.size()), this, val));
        }
    }

    private class ChooseVal extends Resolver {

        private final IVarVal val;

        private ChooseVal(Solver slv, Rational cost, VarFlaw effect, final IVarVal val) {
            super(slv, slv.var_theory.allows(effect.var_item.var, val), cost, effect);
            this.val = val;
        }

        @Override
        void expand() {
        }
    }
}
