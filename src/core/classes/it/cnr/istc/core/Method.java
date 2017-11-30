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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Method extends Scope {

    public final String name;
    final List<Field> arguments;
    final List<Statement> statements;
    final Type return_type;

    public Method(final Core core, final IScope scope, final String name, final Field... args) {
        this(core, scope, name, null, Arrays.asList(args), Collections.emptyList());
    }

    Method(final Core core, final IScope scp, final String name, final Type return_type, final List<Field> args, final List<Statement> statements) {
        super(core, scp);
        this.name = name;
        this.arguments = args;
        if (return_type != null) {
            newFields(new Field(return_type, RETURN, null, true));
        }
        if (scp instanceof Type) {
            newFields(new Field(((Type) scp), THIS, null, true));
        }
        newFields(args.toArray(new Field[args.size()]));
        this.statements = statements;
        this.return_type = return_type;
    }

    public Item invoke(final IEnv env, final Item... args) throws CoreException {
        Env e = new Env(core, env);
        for (int i = 0; i < arguments.size(); i++) {
            e.items.put(arguments.get(i).name, args[i]);
        }

        for (Statement stmnt : statements) {
            stmnt.execute(this, e);
        }

        if (return_type != null) {
            return e.items.get(RETURN);
        } else {
            return null;
        }
    }
}
