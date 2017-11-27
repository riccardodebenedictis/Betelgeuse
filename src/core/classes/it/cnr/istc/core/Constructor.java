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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Constructor extends Scope {

    final List<Field> args;
    final List<Statement> statements;
    final List<Pair<String, List<Expression>>> init_list;

    public Constructor(final Core core, final IScope scope, final Field... args) {
        this(core, scope, Arrays.asList(args), Collections.emptyList(), Collections.emptyList());
    }

    Constructor(final Core core, final IScope scope, final List<Field> args, final List<Statement> statements, final List<Pair<String, List<Expression>>> init_list) {
        super(core, scope);
        this.args = args;
        fields.put(THIS, new Field(((Type) scope), THIS));
        for (Field arg : args) {
            fields.put(arg.name, arg);
        }
        this.statements = statements;
        this.init_list = init_list;
    }
}
