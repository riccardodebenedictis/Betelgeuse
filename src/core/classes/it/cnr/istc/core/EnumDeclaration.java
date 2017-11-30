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

import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class EnumDeclaration extends TypeDeclaration {

    private final List<String> enums;
    private final List<List<String>> type_refs;

    EnumDeclaration(final String name, final List<String> enums, final List<List<String>> type_refs) {
        super(name);
        this.enums = enums;
        this.type_refs = type_refs;
    }

    @Override
    public void declare(final IScope scp) {
        // A new enum type has been declared..
        EnumType et = new EnumType(scp.getCore(), scp, name);

        for (String e : enums) {
            et.instances.add(new Item.StringItem(scp.getCore(), e));
        }

        if (scp instanceof Core) {
            ((Core) scp).newTypes(et);
        } else {
            ((Type) scp).newTypes(et);
        }
    }

    @Override
    public void refine(final IScope scp) {
        if (!type_refs.isEmpty()) {
            EnumType et = (EnumType) scp.getType(name);
            for (List<String> t_ref : type_refs) {
                IScope sc = scp;
                for (String id : t_ref) {
                    sc = sc.getType(id);
                }
                et.enum_types.add((EnumType) sc);
            }
        }
    }
}
