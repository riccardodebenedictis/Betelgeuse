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

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class TypedefDeclaration extends TypeDeclaration {

    private final String primitive_type;
    private final Expression xpr;

    TypedefDeclaration(final String name, final String primitive_type, final Expression xpr) {
        super(name);
        this.primitive_type = primitive_type;
        this.xpr = xpr;
    }

    @Override
    void declare(final IScope scp) {
        assert scp instanceof Core || scp instanceof Type;
        // A new typedef type has been declared..
        TypedefType tt = new TypedefType(scp.getCore(), scp, name, scp.getType(primitive_type), xpr);
        if (scp instanceof Core) {
            ((Core) scp).types.put(name, tt);
        } else if (scp instanceof Type) {
            ((Type) scp).types.put(name, tt);
        }
    }
}
