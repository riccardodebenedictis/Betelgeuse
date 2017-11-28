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
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class DisjunctionStatement implements Statement {

    private final List<Pair<List<Statement>, Expression>> disjunctions;

    DisjunctionStatement(final List<Pair<List<Statement>, Expression>> disjs) {
        this.disjunctions = disjs;
    }

    @Override
    public void execute(IScope scp, IEnv env) throws CoreException {
        List<Conjunction> conjs = new ArrayList<>(disjunctions.size());
        for (Pair<List<Statement>, Expression> conj : disjunctions) {
            conjs.add(new Conjunction(scp.getCore(), scp, ((Item.ArithItem) conj.second.evaluate(scp, env)).l, conj.first));
        }
        scp.getCore().newDisjunction(env, new Disjunction(scp.getCore(), scp, conjs));
    }

    @Override
    public String toString() {
        return "{" + disjunctions.stream().map(dsj -> "[" + dsj.second.toString() + "] " + dsj.first.stream().map(stmnt -> stmnt.toString()).collect(Collectors.joining("\n"))).collect(Collectors.joining("} or {")) + "}";
    }
}
