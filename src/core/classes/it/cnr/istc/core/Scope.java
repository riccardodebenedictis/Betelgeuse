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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Scope implements IScope {

    protected final Core core;
    protected final IScope scope;
    final Map<String, Field> fields = new HashMap<>();

    Scope(final Core core, final IScope scope) {
        this.core = core;
        this.scope = scope;
    }

    @Override
    public Core getCore() {
        return core;
    }

    @Override
    public IScope getScope() {
        return scope;
    }

    @Override
    public Field getField(String name) {
        Field f = fields.get(name);
        if (f != null) {
            return f;
        }
        // if not here, check any enclosing scope
        return scope.getField(name);
    }

    @Override
    public Method getMethod(String name, Type... pars) {
        return scope.getMethod(name, pars);
    }

    @Override
    public Type getType(String name) {
        return scope.getType(name);
    }

    @Override
    public Predicate getPredicate(String name) {
        return scope.getPredicate(name);
    }
}
