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

import it.cnr.istc.common.Pair;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class FormulaStatement extends Statement {

    private final boolean is_fact;
    private final String formula_name;
    private final List<String> formula_scope;
    private final String predicate_name;
    private final List<Pair<String, Expression>> assignments;

    FormulaStatement(final boolean f, final String fn, final List<String> scp, String pn, final List<Pair<String, Expression>> assgns) {
        this.is_fact = f;
        this.formula_name = fn;
        this.formula_scope = scp;
        this.predicate_name = pn;
        this.assignments = assgns;
    }

    @Override
    public void execute(IScope scp, IEnv env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
