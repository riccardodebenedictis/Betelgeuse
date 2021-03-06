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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Scope implements IScope {

    protected final Core core;
    protected final IScope scope;
    private final Map<String, Field> fields = new HashMap<>();

    Scope(final Core core, final IScope scp) {
        this.core = core;
        this.scope = scp;
    }

    @Override
    public Core getCore() {
        return core;
    }

    @Override
    public IScope getScope() {
        return scope;
    }

    protected static void newFields(final Scope csp, final Field... fs) {
        for (Field f : fs) {
            assert !csp.fields.containsKey(f.name);
            csp.fields.put(f.name, f);
        }
    }

    protected final void newFields(final Field... fs) {
        for (Field f : fs) {
            assert !fields.containsKey(f.name);
            fields.put(f.name, f);
        }
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
    public Map<String, Field> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public Method getMethod(String name, Type... pars) {
        return scope.getMethod(name, pars);
    }

    @Override
    public Map<String, Collection<Method>> getMethods() {
        return scope.getMethods();
    }

    @Override
    public Type getType(String name) {
        return scope.getType(name);
    }

    @Override
    public Map<String, Type> getTypes() {
        return scope.getTypes();
    }

    @Override
    public Predicate getPredicate(String name) {
        return scope.getPredicate(name);
    }

    @Override
    public Map<String, Predicate> getPredicates() {
        return scope.getPredicates();
    }
}
