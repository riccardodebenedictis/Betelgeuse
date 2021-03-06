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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class ConjunctionStatement implements Statement {

    private final List<Statement> statements;

    ConjunctionStatement(final Statement... stmnts) {
        this(Arrays.asList(stmnts));
    }

    ConjunctionStatement(final List<Statement> stmnts) {
        this.statements = stmnts;
    }

    @Override
    public void execute(IScope scp, IEnv env) throws CoreException {
        for (Statement stmnt : statements) {
            stmnt.execute(scp, env);
        }
    }

    @Override
    public String toString() {
        return statements.stream().map(stmnt -> stmnt.toString()).collect(Collectors.joining("\n"));
    }
}
