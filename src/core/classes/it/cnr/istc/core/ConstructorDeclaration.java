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
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class ConstructorDeclaration {

    private final Collection<Pair<Collection<String>, String>> parameters;
    private final Collection<Pair<String, Collection<Expression>>> init_list;
    private final Collection<Statement> statements;

    ConstructorDeclaration(final Collection<Pair<Collection<String>, String>> pars, final Collection<Pair<String, Collection<Expression>>> il, final Collection<Statement> stmnts) {
        this.parameters = pars;
        this.init_list = il;
        this.statements = stmnts;
    }

    public void refine(final IScope scp) {
    }
}
