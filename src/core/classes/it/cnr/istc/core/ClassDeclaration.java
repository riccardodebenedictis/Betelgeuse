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
class ClassDeclaration extends TypeDeclaration {

    private final List<FieldDeclaration> fields;
    private final List<List<String>> base_classes;
    private final List<ConstructorDeclaration> constructors;
    private final List<MethodDeclaration> methods;
    private final List<PredicateDeclaration> predicates;
    private final List<TypeDeclaration> types;

    ClassDeclaration(final String name, final List<List<String>> base_classes, final List<FieldDeclaration> fields, final List<ConstructorDeclaration> constructors, final List<MethodDeclaration> methods, final List<PredicateDeclaration> predicates, final List<TypeDeclaration> types) {
        super(name);
        this.base_classes = base_classes;
        this.fields = fields;
        this.constructors = constructors;
        this.methods = methods;
        this.predicates = predicates;
        this.types = types;
    }

    @Override
    void declare(final IScope scp) {
        // A new type has been declared..
        Type tp = new Type(scp.getCore(), scp, name);
        if (scp instanceof Core) {
            ((Core) scp).newTypes(tp);
        } else if (scp instanceof Type) {
            ((Type) scp).newTypes(tp);
        }

        for (TypeDeclaration t : types) {
            t.declare(tp);
        }
    }

    @Override
    void refine(final IScope scp) {
        Type tp = scp.getType(name);

        tp.newSupertypes(base_classes.stream().map(ids -> {
            IScope sc = scp;
            for (String id : ids) {
                sc = sc.getType(id);
            }
            return (Type) sc;
        }).toArray(Type[]::new));

        for (FieldDeclaration f : fields) {
            f.refine(tp);
        }

        if (constructors.isEmpty()) {
            tp.addConstructor(new Constructor(scp.getCore(), tp));
        } else {
            for (ConstructorDeclaration cnstr : constructors) {
                cnstr.refine(tp);
            }
        }

        for (MethodDeclaration md : methods) {
            md.refine(tp);
        }
        for (PredicateDeclaration pd : predicates) {
            pd.refine(tp);
        }
        for (TypeDeclaration t : types) {
            t.refine(tp);
        }
    }
}
