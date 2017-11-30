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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class PredicateDeclaration {

    private final String name;
    private final List<Pair<List<String>, String>> parameters;
    private final List<List<String>> predicate_list;
    private final List<Statement> statements;

    PredicateDeclaration(final String n, final List<Pair<List<String>, String>> pars, final List<List<String>> pl, final List<Statement> stmnts) {
        this.name = n;
        this.parameters = pars;
        this.predicate_list = pl;
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

        Predicate p = new Predicate(scp.getCore(), scp, name, args, statements);

        p.newSupertypes(predicate_list.stream().map(ids -> {
            IScope sc = scp;
            for (String id : ids) {
                sc = sc.getType(id);
            }
            return (Predicate) sc;
        }).toArray(Predicate[]::new));

        if (scp instanceof Core) {
            ((Core) scp).newPredicates(p);
        } else {
            ((Type) scp).newPredicates(p);
            Queue<Type> q = new ArrayDeque<>();
            q.add((Type) scp);
            while (!q.isEmpty()) {
                Type tp = q.poll();
                tp.newPredicate(p);
                q.addAll(tp.getSupertypes());
            }
        }
    }
}
