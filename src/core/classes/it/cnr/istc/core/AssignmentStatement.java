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

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class AssignmentStatement implements Statement {

    private final List<String> ids;
    private final String id;
    private final Expression xpr;

    public AssignmentStatement(final List<String> ids, final String id, final Expression xpr) {
        this.ids = ids;
        this.id = id;
        this.xpr = xpr;
    }

    @Override
    public void execute(IScope scp, IEnv env) throws CoreException {
        IEnv e = env;
        for (String id : ids) {
            e = e.get(id);
        }
        if (e instanceof Core) {
            ((Core) e).items.put(id, xpr.evaluate(scp, env));
        } else {
            ((Env) e).items.put(id, xpr.evaluate(scp, env));
        }
    }

    @Override
    public String toString() {
        return ids.stream().collect(Collectors.joining(".")) + "." + id + " = " + xpr.toString() + ";";
    }
}
