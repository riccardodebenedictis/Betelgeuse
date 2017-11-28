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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Predicate extends Type {

    final List<Field> arguments;
    private final List<Statement> statements;

    public Predicate(final Core core, final IScope scope, final String name, final Field... args) {
        this(core, scope, name, Arrays.asList(args), Collections.emptyList());
    }

    Predicate(final Core core, final IScope scp, final String name, final List<Field> args, final List<Statement> statements) {
        super(core, scp, name);
        this.arguments = args;
        if (scp instanceof Type) {
            fields.put(THIS, new Field((Type) scp, THIS, null, true));
        }
        for (Field arg : args) {
            fields.put(arg.name, arg);
        }
        this.statements = statements;
    }

    @Override
    public Atom newInstance(IEnv env) throws CoreException {
        Atom atm = new Atom(core, env, this);
        Queue<Type> q = new ArrayDeque<>();
        q.add(this);
        while (!q.isEmpty()) {
            Type pred = q.poll();
            pred.instances.add(atm);
            q.addAll(pred.supertypes);
        }
        return atm;
    }

    public void applyRule(final Atom atom) throws CoreException {
        for (Type st : supertypes) {
            ((Predicate) st).applyRule(atom);
        }
        Env c_env = new Env(core, atom);
        c_env.items.put(THIS, atom);
        for (Statement stmnt : statements) {
            stmnt.execute(this, c_env);
        }
    }
}
