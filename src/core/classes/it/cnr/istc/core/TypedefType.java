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
class TypedefType extends Type {

    private final Type base_type;
    private final Expression xpr;

    TypedefType(Core core, IScope scope, String name, final Type base_type, final Expression xpr) {
        super(core, scope, name);
        this.base_type = base_type;
        this.xpr = xpr;
    }

    @Override
    public Item newInstance(IEnv env) throws CoreException {
        return xpr.evaluate(this, env);
    }
}
