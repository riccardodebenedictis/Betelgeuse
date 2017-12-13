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

import it.cnr.istc.common.Pair;
import static it.cnr.istc.core.IScope.TAU;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.var.IVarVal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class FormulaStatement implements Statement {

    private final boolean is_fact;
    private final String formula_name;
    private final List<String> formula_scope;
    private final String predicate_name;
    private final List<Pair<String, Expression>> assignments;

    FormulaStatement(final boolean f, final String fn, final List<String> scp, String pn, final List<Pair<String, Expression>> assgns) {
        this.is_fact = f;
        this.formula_name = fn;
        this.formula_scope = scp;
        this.predicate_name = pn;
        this.assignments = assgns;
    }

    @Override
    public void execute(IScope scp, IEnv env) throws CoreException {
        Predicate p;
        Map<String, Item> assignmnts = new HashMap<>();
        if (!formula_scope.isEmpty()) {
            // the scope is explicitely declared..
            IEnv e = env;
            for (String id : formula_scope) {
                e = e.get(id);
            }
            assignmnts.put(TAU, (Item) e);
            p = ((Item) e).type.getPredicate(predicate_name);
        } else {
            // we inherit the scope..
            p = scp.getPredicate(predicate_name);
            if (p.getScope() != p.getCore()) {
                assignmnts.put(TAU, env.get(TAU));
            }
        }

        for (Pair<String, Expression> asgnmnt : assignments) {
            Item xpr = asgnmnt.second.evaluate(scp, env);
            Type tp = p.getField(asgnmnt.first).type; // the target type..
            if (tp.isAssignableFrom(xpr.type)) {
                // the target type is a superclass of the assignment..
                assignmnts.put(asgnmnt.first, xpr);
            } else if (xpr.type.isAssignableFrom(tp)) {
                // the target type is a subclass of the assignment..
                if (xpr instanceof Item.VarItem) {
                    // some of the allowed values might be inhibited..
                    Set<IVarVal> alwd_vals = scp.getCore().var_theory.value(((Item.VarItem) xpr).var);
                    Set<Lit> not_alwd_vals = new HashSet<>();
                    for (IVarVal val : alwd_vals) {
                        if (!tp.isAssignableFrom(((Item) val).type)) {// the target type is not a superclass of the value..
                            not_alwd_vals.add(new Lit(scp.getCore().var_theory.allows(((Item.VarItem) xpr).var, val), false));
                        }
                    }

                    if (alwd_vals.size() == not_alwd_vals.size()) {
                        // none of the values is allowed..
                        throw new InconsistencyException();
                    } else {
                        // no need to go further..
                        scp.getCore().assertFacts(not_alwd_vals.toArray(new Lit[not_alwd_vals.size()])); // we inhibit the not allowed values..
                    }
                } else {
                    throw new InconsistencyException();
                }
            } else {
                throw new InconsistencyException();
            }
        }

        Atom a;
        Item tau = assignmnts.get(TAU);
        if (tau != null) {
            // we have computed the new atom's scope above..
            a = p.newInstance(tau);
        } else {
            // the new atom's scope is the core..
            a = p.newInstance(scp.getCore());
        }

        // we assign fields..
        a.items.putAll(assignmnts);

        Deque<Predicate> q = new ArrayDeque<>();
        q.addLast(p);
        while (!q.isEmpty()) {
            Predicate pred = q.pollFirst();
            for (Field arg : pred.arguments) {
                if (!a.items.containsKey(arg.name)) {
                    // the field is uninstantiated..
                    if (arg.type.primitive) {
                        a.items.put(arg.name, arg.type.newInstance(env));
                    } else {
                        a.items.put(arg.name, arg.type.newExistential());
                    }
                }
            }
            for (Type st : pred.getSupertypes()) {
                q.add((Predicate) st);
            }
        }

        if (is_fact) {
            scp.getCore().newFact(a);
        } else {
            scp.getCore().newGoal(a);
        }

        if (env instanceof Core) {
            ((Core) env).items.put(formula_name, a);
        } else {
            ((Env) env).items.put(formula_name, a);
        }
    }

    @Override
    public String toString() {
        return (is_fact ? "fact " : "goal ") + formula_name + " = new " + formula_scope.stream().collect(Collectors.joining(".")) + (formula_scope.isEmpty() ? "" : ".") + predicate_name + "(" + assignments.stream().map(assgnmnt -> assgnmnt.first + ": " + assgnmnt.second.toString()).collect(Collectors.joining(", ")) + ");";
    }
}
