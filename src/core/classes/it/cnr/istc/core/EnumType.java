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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class EnumType extends Type {

    final Collection<EnumType> enum_types = new ArrayList<>();

    EnumType(Core core, IScope scope, String name) {
        super(core, scope, name);
    }

    @Override
    public Item newInstance(IEnv ctx) throws CoreException {
        return core.newEnum(this, getAllInstances());
    }

    private Set<Item> getAllInstances() {
        Set<Item> itms = new HashSet<>(instances);
        for (EnumType et : enum_types) {
            itms.addAll(et.getAllInstances());
        }
        return itms;
    }
}
