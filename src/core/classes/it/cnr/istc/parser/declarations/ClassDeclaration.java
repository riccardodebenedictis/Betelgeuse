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
import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class ClassDeclaration extends TypeDeclaration {

    private final Collection<FieldDeclaration> fields;
    private final Collection<Collection<String>> base_classes;
    private final Collection<ConstructorDeclaration> constructors;
    private final Collection<MethodDeclaration> methods;
    private final Collection<PredicateDeclaration> predicates;
    private final Collection<TypeDeclaration> types;

    public ClassDeclaration(final String name, final Collection<Collection<String>> base_classes, final Collection<FieldDeclaration> fields, final Collection<ConstructorDeclaration> constructors, final Collection<MethodDeclaration> methods, final Collection<PredicateDeclaration> predicates, final Collection<TypeDeclaration> types) {
        super(name);
        this.base_classes = base_classes;
        this.fields = fields;
        this.constructors = constructors;
        this.methods = methods;
        this.predicates = predicates;
        this.types = types;
    }

    @Override
    public void declare(final IScope scp) {
    }

    @Override
    public void refine(final IScope scp) {
    }
}
