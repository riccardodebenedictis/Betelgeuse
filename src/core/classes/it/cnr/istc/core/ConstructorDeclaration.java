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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class ConstructorDeclaration {

    private final List<Pair<List<String>, String>> parameters;
    private final List<Pair<String, List<Expression>>> init_list;
    private final List<Statement> statements;

    ConstructorDeclaration(final List<Pair<List<String>, String>> pars, final List<Pair<String, List<Expression>>> il, final List<Statement> stmnts) {
        this.parameters = pars;
        this.init_list = il;
        this.statements = stmnts;
    }

    void refine(final IScope scp) {
        List<Field> args = new ArrayList<>(parameters.size());
        for (Pair<List<String>, String> par : parameters) {
            IScope sc = scp;
            for (String id : par.first) {
                sc = sc.getType(id);
            }
            args.add(new Field((Type) sc, par.second));
        }

        ((Type) scp).newConstructors(new Constructor(scp.getCore(), scp, args, statements, init_list));
    }
}
