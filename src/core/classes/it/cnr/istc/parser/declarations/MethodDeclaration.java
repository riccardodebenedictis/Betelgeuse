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
package it.cnr.istc.parser.declarations;

import it.cnr.istc.common.Pair;
import it.cnr.istc.core.IScope;
import it.cnr.istc.parser.statements.Statement;
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class MethodDeclaration {

    private final Collection<String> return_type;
    private final String name;
    private final Collection<Pair<Collection<String>, String>> parameters;
    private final Collection<Statement> statements;

    public MethodDeclaration(final Collection<String> ids, final String n, final Collection<Pair<Collection<String>, String>> pars, final Collection<Statement> stmnts) {
        this.return_type = ids;
        this.name = n;
        this.parameters = pars;
        this.statements = stmnts;
    }

    public void refine(final IScope scp) {
    }
}
