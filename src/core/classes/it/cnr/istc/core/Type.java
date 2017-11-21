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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Type extends Scope {

    public final String name;
    public final boolean primitive;
    final Collection<Type> supertypes = new ArrayList<>();
    final Map<String, Collection<Method>> methods = new HashMap<>();
    final Map<String, Type> types = new HashMap<>();
    final Map<String, Predicate> predicates = new HashMap<>();

    public Type(Core core, IScope scope, final String name) {
        this(core, scope, name, false);
    }

    Type(Core core, IScope scope, final String name, final boolean primitive) {
        super(core, scope);
        this.name = name;
        this.primitive = primitive;
    }

    public boolean isAssignableFrom(final Type t) {
        Queue<Type> q = new ArrayDeque<>();
        q.add(t);
        while (!q.isEmpty()) {
            if (q.peek() == this) {
                return true;
            } else {
                q.addAll(q.peek().supertypes);
                q.poll();
            }
        }
        return false;
    }

    @Override
    public Field getField(String name) {
        Field f = fields.get(name);
        if (f != null) {
            return f;
        }
        // if not here, check any enclosing scope
        try {
            return scope.getField(name);
        } catch (NoSuchFieldError e0) {
            for (Type st : supertypes) {
                try {
                    return st.getField(name);
                } catch (NoSuchFieldError e1) {
                }
            }
        }
        throw new NoSuchFieldError(name);
    }

    @Override
    public Method getMethod(String name, Type... pars) {
        Collection<Method> c_ms = methods.get(name);
        if (c_ms != null) {
            for (Method mthd : c_ms) {
                if (mthd.args.length == pars.length) {
                    boolean found = true;
                    for (int i = 0; i < pars.length; i++) {
                        if (!mthd.args[i].type.isAssignableFrom(pars[i])) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        return mthd;
                    }
                }
            }
        }
        // if not here, check any enclosing scope
        try {
            return scope.getMethod(name, pars);
        } catch (NoSuchMethodError e0) {
            for (Type st : supertypes) {
                try {
                    return st.getMethod(name, pars);
                } catch (NoSuchMethodError e1) {
                }
            }
        }
        throw new NoSuchMethodError(name);
    }

    @Override
    public Type getType(String name) {
        Type tp = types.get(name);
        if (tp != null) {
            return tp;
        }
        // if not here, check any enclosing scope
        try {
            return scope.getType(name);
        } catch (NoClassDefFoundError e0) {
            for (Type st : supertypes) {
                try {
                    return st.getType(name);
                } catch (NoClassDefFoundError e1) {
                }
            }
        }
        throw new NoClassDefFoundError(name);
    }

    @Override
    public Predicate getPredicate(String name) {
        Predicate p = predicates.get(name);
        if (p != null) {
            return p;
        }
        // if not here, check any enclosing scope
        try {
            return scope.getPredicate(name);
        } catch (NoClassDefFoundError e0) {
            for (Type st : supertypes) {
                try {
                    return st.getPredicate(name);
                } catch (NoClassDefFoundError e1) {
                }
            }
        }
        throw new NoClassDefFoundError(name);
    }
}
