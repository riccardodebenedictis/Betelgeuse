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

import java.util.Collection;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class CompilationUnit {

    private final Collection<MethodDeclaration> methods;
    private final Collection<PredicateDeclaration> predicates;
    private final Collection<TypeDeclaration> types;
    private final Collection<Statement> statements;

    CompilationUnit(final Collection<MethodDeclaration> methods, final Collection<PredicateDeclaration> predicates, final Collection<TypeDeclaration> types, final Collection<Statement> statements) {
        this.methods = methods;
        this.predicates = predicates;
        this.types = types;
        this.statements = statements;
    }

    public void declare(final IScope scp) {
        for (TypeDeclaration type : types) {
            type.declare(scp);
        }
    }

    public void refine(final IScope scp) {
        for (MethodDeclaration method : methods) {
            method.refine(scp);
        }
        for (PredicateDeclaration predicate : predicates) {
            predicate.refine(scp);
        }
        for (TypeDeclaration type : types) {
            type.refine(scp);
        }
    }

    public void execute(final IScope scp, final IEnv env) {
        for (Statement statement : statements) {
            statement.execute(scp, env);
        }
    }
}
