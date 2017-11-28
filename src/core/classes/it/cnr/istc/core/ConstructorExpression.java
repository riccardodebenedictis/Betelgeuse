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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class ConstructorExpression implements Expression {

    private final Collection<String> type;
    private final Collection<Expression> xprs;

    ConstructorExpression(final Collection<String> ids, final Collection<Expression> xprs) {
        this.type = ids;
        this.xprs = xprs;
    }

    @Override
    public Item evaluate(IScope scp, IEnv env) throws CoreException {
        IScope sc = scp;
        for (String id : type) {
            sc = sc.getType(id);
        }

        List<Item> args = new ArrayList<>(xprs.size());
        List<Type> par_types = new ArrayList<>(xprs.size());
        for (Expression xpr : xprs) {
            Item arg = xpr.evaluate(scp, env);
            args.add(arg);
            par_types.add(arg.type);
        }

        return ((Type) sc).getConstructor(par_types.toArray(new Type[par_types.size()])).newInstance(env, args.toArray(new Item[args.size()]));
    }

    @Override
    public String toString() {
        return "new " + type.stream().collect(Collectors.joining(".")) + "(" + xprs.stream().map(xpr -> xpr.toString()).collect(Collectors.joining(", ")) + ")";
    }
}
