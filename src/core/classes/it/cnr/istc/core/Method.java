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

import it.cnr.istc.parser.statements.Statement;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Method extends Scope {

    public final String name;
    final Field[] args;
    final Statement[] statements;
    final Type return_type;

    public Method(final Core core, final IScope scope, final String name, final Field[] args, final Statement[] statements) {
        this(core, scope, name, args, statements, null);
    }

    public Method(final Core core, final IScope scope, final String name, final Field[] args, final Statement[] statements, final Type return_type) {
        super(core, scope);
        this.name = name;
        this.args = args;
        fields.put(THIS, new Field(((Type) scope), THIS));
        if (return_type != null) {
            fields.put(RETURN, new Field(return_type, RETURN));
        }
        for (Field arg : args) {
            fields.put(arg.name, arg);
        }
        this.statements = statements;
        this.return_type = return_type;
    }
}
