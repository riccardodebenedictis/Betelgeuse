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
package it.cnr.istc.solver;

import it.cnr.istc.core.Atom;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Field;
import it.cnr.istc.core.IScope;
import it.cnr.istc.core.Item;
import it.cnr.istc.core.Type;
import it.cnr.istc.smt.SatValueListener;
import it.cnr.istc.smt.lra.LRAValueListener;
import it.cnr.istc.smt.lra.Rational;
import it.cnr.istc.smt.var.VarValueListener;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public abstract class SmartType extends Type {

    public SmartType(final Solver slv, final IScope scope, final String name) {
        super(slv, scope, name);
    }

    public abstract Collection<Flaw> getFlaws();

    protected void newFact(final SupportFlaw f) throws CoreException {
    }

    protected void newGoal(final SupportFlaw f) throws CoreException {
    }

    protected static abstract class AtomListener implements SatValueListener, LRAValueListener, VarValueListener {

        protected final Atom atom;

        protected AtomListener(final Atom atom) {
            this.atom = atom;
            Queue<Type> q = new ArrayDeque<>();
            q.add((Type) atom.type.getScope());
            while (!q.isEmpty()) {
                Type tp = q.poll();
                for (Map.Entry<String, Field> field : tp.getFields().entrySet()) {
                    if (!field.getValue().synthetic) {
                        switch (field.getValue().name) {
                            case Type.BOOL:
                                atom.getCore().sat_core.listen(((Item.BoolItem) atom.get(field.getKey())).l.v, this);
                                break;
                            case Type.INT:
                            case Type.REAL:
                                for (Map.Entry<Integer, Rational> term : ((Item.ArithItem) atom.get(field.getKey())).l.vars.entrySet()) {
                                    atom.getCore().la_theory.listen(term.getKey(), this);
                                }
                                break;
                            case Type.STRING:
                                break;
                            default:
                                atom.getCore().var_theory.listen(((Item.VarItem) atom.get(field.getKey())).var, this);
                        }
                    }
                }
                q.addAll(tp.getSupertypes());
            }
        }
    }
}
