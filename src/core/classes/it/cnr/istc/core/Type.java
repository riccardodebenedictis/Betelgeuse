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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Type extends Scope {

    public static final String BOOL = "bool";
    public static final String INT = "int";
    public static final String REAL = "real";
    public static final String STRING = "string";
    public final String name;
    public final boolean primitive;
    private final Collection<Type> supertypes = new ArrayList<>();
    private final Collection<Constructor> constructors = new ArrayList<>();
    private final Map<String, Collection<Method>> methods = new HashMap<>();
    private final Map<String, Type> types = new HashMap<>();
    private final Map<String, Predicate> predicates = new HashMap<>();
    final Collection<Item> instances = new ArrayList<>();

    public Type(final Core core, final IScope scp, final String name) {
        this(core, scp, name, false);
    }

    Type(final Core core, final IScope scope, final String name, final boolean primitive) {
        super(core, scope);
        this.name = name;
        this.primitive = primitive;
    }

    protected static void newSupertypes(final Type base, final Type... super_types) {
        base.supertypes.addAll(Arrays.asList(super_types));
    }

    protected void newSupertypes(final Type... super_types) {
        supertypes.addAll(Arrays.asList(super_types));
    }

    public Collection<Type> getSupertypes() {
        return Collections.unmodifiableCollection(supertypes);
    }

    public Collection<Item> getInstances() {
        return Collections.unmodifiableCollection(instances);
    }

    public boolean isAssignableFrom(final Type t) {
        Queue<Type> q = new ArrayDeque<>();
        q.add(t);
        while (!q.isEmpty()) {
            Type tp = q.poll();
            if (tp == this) {
                return true;
            }
            q.addAll(tp.supertypes);
        }
        return false;
    }

    protected void newPredicate(final Predicate p) {
    }

    protected void addConstructor(final Constructor constructor) {
        constructors.add(constructor);
    }

    public Constructor getConstructor(final Type... pars) {
        for (Constructor cstr : constructors) {
            if (cstr.arguments.size() == pars.length) {
                boolean found = true;
                for (int i = 0; i < pars.length; i++) {
                    if (!cstr.arguments.get(i).type.isAssignableFrom(pars[i])) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return cstr;
                }
            }
        }
        throw new NoSuchMethodError();
    }

    @Override
    public Field getField(final String name) {
        try {
            return super.getField(name);
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

    protected void newMethods(final Method... ms) {
        for (Method m : ms) {
            Collection<Method> c_ms = methods.get(m.name);
            if (c_ms == null) {
                c_ms = new ArrayList<>();
                methods.put(m.name, c_ms);
            }
            assert !methods.get(m.name).contains(m);
            c_ms.add(m);
        }
    }

    @Override
    public Method getMethod(final String name, final Type... pars) {
        Collection<Method> c_ms = methods.get(name);
        if (c_ms != null) {
            for (Method mthd : c_ms) {
                if (mthd.arguments.size() == pars.length) {
                    boolean found = true;
                    for (int i = 0; i < pars.length; i++) {
                        if (!mthd.arguments.get(i).type.isAssignableFrom(pars[i])) {
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
    public Map<String, Collection<Method>> getMethods() {
        Map<String, Collection<Method>> c_methods = new HashMap<>();
        for (Map.Entry<String, Collection<Method>> ms : methods.entrySet()) {
            c_methods.put(ms.getKey(), Collections.unmodifiableCollection(ms.getValue()));
        }
        return Collections.unmodifiableMap(c_methods);
    }

    protected final void newTypes(final Type... ts) {
        for (Type t : ts) {
            assert !types.containsKey(t.name);
            types.put(t.name, t);
        }
    }

    @Override
    public Type getType(final String name) {
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
    public Map<String, Type> getTypes() {
        return Collections.unmodifiableMap(types);
    }

    protected final void newPredicates(final Predicate... ps) {
        for (Predicate p : ps) {
            assert !predicates.containsKey(p.name);
            predicates.put(p.name, p);
        }
    }

    @Override
    public Predicate getPredicate(final String name) {
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

    @Override
    public Map<String, Predicate> getPredicates() {
        return Collections.unmodifiableMap(predicates);
    }

    public Item newInstance(final IEnv env) throws CoreException {
        Item i = new Item(core, env, this);
        Queue<Type> q = new ArrayDeque<>();
        q.add(this);
        while (!q.isEmpty()) {
            Type type = q.poll();
            type.instances.add(i);
            q.addAll(type.supertypes);
        }
        return i;
    }

    public Item newExistential() throws CoreException {
        return core.newEnum(this, new HashSet<>(instances));
    }

    protected void setVar(final int v) {
        core.setVar(v);
    }

    protected void restoreVar() {
        core.restoreVar();
    }

    static class BoolType extends Type {

        BoolType(final Core core) {
            super(core, core, BOOL, true);
        }

        @Override
        public Item newInstance(final IEnv ctx) {
            return core.newBool();
        }
    }

    static class IntType extends Type {

        IntType(final Core core) {
            super(core, core, INT, true);
        }

        @Override
        public Item newInstance(final IEnv ctx) {
            return core.newInt();
        }
    }

    static class RealType extends Type {

        RealType(final Core core) {
            super(core, core, REAL, true);
        }

        @Override
        public Item newInstance(final IEnv ctx) {
            return core.newReal();
        }
    }

    static class StringType extends Type {

        StringType(final Core core) {
            super(core, core, STRING, true);
        }

        @Override
        public Item newInstance(final IEnv ctx) {
            return core.newString();
        }
    }
}
