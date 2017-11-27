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
class MethodDeclaration {

    private final List<String> return_type;
    private final String name;
    private final List<Pair<List<String>, String>> parameters;
    private final List<Statement> statements;

    MethodDeclaration(final List<String> ids, final String n, final List<Pair<List<String>, String>> pars, final List<Statement> stmnts) {
        this.return_type = ids;
        this.name = n;
        this.parameters = pars;
        this.statements = stmnts;
    }

    public void refine(final IScope scp) {
    }
}
