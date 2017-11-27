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

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Predicate extends Type {

    final List<Field> arguments;

    public Predicate(final Core core, final IScope scope, final String name, final Field... args) {
        this(core, scope, name, Arrays.asList(args));
    }

    Predicate(final Core core, final IScope scp, final String name, final List<Field> args) {
        super(core, scp, name);
        this.arguments = args;
    }

    @Override
    public Atom newInstance(IEnv ctx) throws CoreException {
        throw new UnsupportedOperationException("not supported yet..");
    }
}
