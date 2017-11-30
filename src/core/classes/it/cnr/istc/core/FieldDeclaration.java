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
class FieldDeclaration {

    private final List<String> field_type;
    private final List<VariableDeclaration> declarations;

    FieldDeclaration(final List<String> tp, final List<VariableDeclaration> ds) {
        this.field_type = tp;
        this.declarations = ds;
    }

    void refine(final IScope scp) {
        // we add fields to the current scope..
        IScope sc = scp;
        for (String id : field_type) {
            sc = sc.getType(id);
        }
        Type tp = (Type) sc;
        if (scp instanceof Core) {
            ((Core) scp).newFields(declarations.stream().map(dec -> new Field(tp, dec.name, dec.expression, false)).toArray(Field[]::new));
        } else {
            ((Scope) scp).newFields(declarations.stream().map(dec -> new Field(tp, dec.name, dec.expression, false)).toArray(Field[]::new));
        }
    }
}
