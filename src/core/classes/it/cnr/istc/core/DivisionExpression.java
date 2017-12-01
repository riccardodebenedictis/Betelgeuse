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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class DivisionExpression implements Expression {

    private final List<Expression> xprs;

    public DivisionExpression(final Expression... xprs) {
        this(Arrays.asList(xprs));
    }

    public DivisionExpression(final List<Expression> xprs) {
        this.xprs = xprs;
    }

    @Override
    public Item evaluate(IScope scp, IEnv env) throws CoreException {
        List<Item.ArithItem> itms = new ArrayList<>(xprs.size());
        for (Expression xpr : xprs) {
            itms.add((Item.ArithItem) xpr.evaluate(scp, env));
        }
        return scp.getCore().div(itms.toArray(new Item.ArithItem[itms.size()]));
    }

    @Override
    public String toString() {
        return xprs.stream().map(xpr -> xpr.toString()).collect(Collectors.joining(" / "));
    }
}
