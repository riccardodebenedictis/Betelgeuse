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
package it.cnr.istc.parser.statements;

import it.cnr.istc.core.IEnv;
import it.cnr.istc.core.IScope;
import it.cnr.istc.parser.expressions.Expression;
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class LocalFieldStatement extends Statement {

    private final Collection<String> field_type;
    private final String name;
    private final Expression xpr;

    public LocalFieldStatement(final Collection<String> ids, final String n, final Expression e) {
        this.field_type = ids;
        this.name = n;
        this.xpr = e;
    }

    @Override
    public void execute(IScope scp, IEnv env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
