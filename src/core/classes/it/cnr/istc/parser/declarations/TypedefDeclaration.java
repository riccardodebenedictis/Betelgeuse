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
package it.cnr.istc.parser.declarations;

import it.cnr.istc.core.IScope;
import it.cnr.istc.parser.expressions.Expression;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class TypedefDeclaration extends TypeDeclaration {

    private final String name;
    private final String primitive_type;
    private final Expression xpr;

    public TypedefDeclaration(final String name, final String primitive_type, final Expression xpr) {
        this.name = name;
        this.primitive_type = primitive_type;
        this.xpr = xpr;
    }

    @Override
    public void declare(final IScope scp) {
    }
}
