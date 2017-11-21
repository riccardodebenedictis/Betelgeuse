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

import it.cnr.istc.smt.SatCore;
import it.cnr.istc.smt.lra.LRATheory;
import it.cnr.istc.smt.var.VarTheory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Core implements IScope, IEnv {

    public final SatCore sat_core = new SatCore();
    public final LRATheory la_theory = new LRATheory(sat_core);
    public final VarTheory var_theory = new VarTheory(sat_core);
    final Map<String, Field> fields = new HashMap<>();
    final Map<String, Collection<Method>> methods = new HashMap<>();
    final Map<String, Type> types = new HashMap<>();
    final Map<String, Predicate> predicates = new HashMap<>();
    final Map<String, Item> items = new HashMap<>();

    @Override
    public Core getCore() {
        return this;
    }

    @Override
    public IScope getScope() {
        return null;
    }

    @Override
    public Field getField(String name) {
        Field f = fields.get(name);
        if (f != null) {
            return f;
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
        throw new NoSuchMethodError(name);
    }

    @Override
    public Type getType(String name) {
        Type tp = types.get(name);
        if (tp != null) {
            return tp;
        }
        throw new NoClassDefFoundError(name);
    }

    @Override
    public Predicate getPredicate(String name) {
        Predicate p = predicates.get(name);
        if (p != null) {
            return p;
        }
        throw new NoClassDefFoundError(name);
    }

    @Override
    public IEnv getEnv() {
        return null;
    }

    @Override
    public <T extends Item> T get(String name) {
        T it = (T) items.get(name);
        if (it != null) {
            return it;
        }
        throw new NoSuchFieldError(name);
    }
}
