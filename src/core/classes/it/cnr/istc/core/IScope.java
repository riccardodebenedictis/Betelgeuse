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
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public interface IScope {

    public static final String THIS = "this";
    public static final String RETURN = "return";
    public static final String TAU = "τ";

    public Core getCore();

    public IScope getScope();

    public Field getField(final String name);

    public Map<String, Field> getFields();

    public Method getMethod(final String name, final Type... pars);

    public Map<String, Collection<Method>> getMethods();

    public Type getType(final String name);

    public Map<String, Type> getTypes();

    public Predicate getPredicate(final String name);

    public Map<String, Predicate> getPredicates();
}
