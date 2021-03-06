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
package it.cnr.istc.solver.types;

import it.cnr.istc.common.CombinationGenerator;
import it.cnr.istc.core.Atom;
import it.cnr.istc.core.AtomListener;
import it.cnr.istc.core.CoreException;
import static it.cnr.istc.core.IScope.TAU;
import it.cnr.istc.core.Item;
import it.cnr.istc.core.Type;
import it.cnr.istc.core.UnsolvableException;
import static it.cnr.istc.smt.LBool.True;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.lra.InfRational;
import it.cnr.istc.smt.lra.Rational;
import it.cnr.istc.smt.var.IVarVal;
import it.cnr.istc.solver.Flaw;
import it.cnr.istc.solver.Resolver;
import it.cnr.istc.solver.SmartType;
import it.cnr.istc.solver.Solver;
import it.cnr.istc.solver.SupportFlaw;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class ReusableResource extends SmartType {

    public static final String REUSABLE_RESOURCE = "ReusableResource";
    public static final String REUSABLE_RESOURCE_CAPACITY = "capacity";
    public static final String REUSABLE_RESOURCE_USE = "Use";
    public static final String REUSABLE_RESOURCE_AMOUNT = "amount";
    private final Collection<Atom> atoms = new ArrayList<>();
    private Collection<Item> to_check = new HashSet<>();

    public ReusableResource(final Solver slv) {
        super(slv, slv, REUSABLE_RESOURCE);
        try {
            // we add the constructor..
            read(REUSABLE_RESOURCE + "(" + Type.REAL + " " + REUSABLE_RESOURCE_CAPACITY + "): " + REUSABLE_RESOURCE_CAPACITY + "(" + REUSABLE_RESOURCE_CAPACITY + ") {" + REUSABLE_RESOURCE_CAPACITY + " >= 0;}");
            // we add the use predicate..
            read("predicate " + REUSABLE_RESOURCE_USE + "(" + Type.REAL + " " + REUSABLE_RESOURCE_AMOUNT + ") : IntervalPredicate + {" + REUSABLE_RESOURCE_AMOUNT + " >= 0;}");
        } catch (CoreException ex) {
            throw new AssertionError();
        }
    }

    @Override
    protected void newFact(SupportFlaw f) throws CoreException {
        // we apply interval-predicate if the fact becomes active..
        setVar(f.atom.sigma);
        core.getPredicate("IntervalPredicate").applyRule(f.atom);
        restoreVar();

        // we avoid unification..
        if (!core.sat_core.newClause(new Lit(f.getPhi(), false), new Lit(f.atom.sigma))) {
            throw new UnsolvableException();
        }

        atoms.add(f.atom);
        getCore().listen(f.atom, new AtomListener() {
            @Override
            public void satValueChange(int v) {
                atom_changed(f.atom);
            }

            @Override
            public void lraValueChange(int v) {
                atom_changed(f.atom);
            }

            @Override
            public void varValueChange(int v) {
                atom_changed(f.atom);
            }
        });
        atom_changed(f.atom);
    }

    @Override
    protected void newGoal(SupportFlaw f) throws CoreException {
        throw new AssertionError("it is not possible to create goals on reusable resources..");
    }

    private void atom_changed(final Atom atm) {
        Item tau = atm.get(TAU);
        if (tau instanceof Item.VarItem) {
            to_check.addAll(core.var_theory.value(((Item.VarItem) tau).var).stream().map(var_val -> (Item) var_val).collect(Collectors.toList()));
        } else {
            to_check.add(tau);
        }
    }

    @Override
    public Collection<Flaw> getFlaws() {
        if (to_check.isEmpty()) {
            // nothing has changed since last inconsistency check..
            return Collections.emptyList();
        } else {
            // we collect atoms for each state variable..
            Map<Item, Collection<Atom>> rr_atoms = new IdentityHashMap<>();
            for (Atom atom : atoms) {
                // we filter out those which are not strictly active..
                if (core.sat_core.value(atom.sigma) == True) {
                    Item tau = atom.get(TAU);
                    if (tau instanceof Item.VarItem) {
                        for (IVarVal val : core.var_theory.value(((Item.VarItem) tau).var)) {
                            Item c_val = (Item) val;
                            if (to_check.contains(c_val)) {
                                Collection<Atom> atms = rr_atoms.get(c_val);
                                if (atms == null) {
                                    atms = new ArrayList<>();
                                    rr_atoms.put(c_val, atms);
                                }
                                atms.add(atom);
                            }
                        }
                    } else if (to_check.contains(tau)) {
                        Collection<Atom> atms = rr_atoms.get(tau);
                        if (atms == null) {
                            atms = new ArrayList<>();
                            rr_atoms.put(tau, atms);
                        }
                        atms.add(atom);
                    }
                }
            }
            to_check.clear();

            // we collect the peaks..
            Collection<Flaw> peaks = new ArrayList<>();
            for (Map.Entry<Item, Collection<Atom>> c_atoms : rr_atoms.entrySet()) {
                // for each pulse, the atoms starting at that pulse..
                Map<InfRational, Collection<Atom>> starting_atoms = new HashMap<>();
                // for each pulse, the atoms ending at that pulse..
                Map<InfRational, Collection<Atom>> ending_atoms = new HashMap<>();;
                // all the pulses of the timeline..
                Set<InfRational> pulses = new TreeSet<>();

                for (Atom atom : c_atoms.getValue()) {
                    Item.ArithItem start_var = atom.get("start");
                    Item.ArithItem end_var = atom.get("end");
                    InfRational start_val = core.value(start_var);
                    InfRational end_val = core.value(end_var);
                    Collection<Atom> start_atms = starting_atoms.get(start_val);
                    if (start_atms == null) {
                        start_atms = new ArrayList<>();
                        starting_atoms.put(start_val, start_atms);
                    }
                    start_atms.add(atom);
                    Collection<Atom> end_atms = ending_atoms.get(end_val);
                    if (end_atms == null) {
                        end_atms = new ArrayList<>();
                        ending_atoms.put(end_val, end_atms);
                    }
                    end_atms.add(atom);
                    pulses.add(start_val);
                    pulses.add(end_val);
                }

                Collection<Atom> overlapping_atoms = new HashSet<>();
                Item.ArithItem capacity_var = c_atoms.getKey().get(REUSABLE_RESOURCE_CAPACITY);
                InfRational capacity_val = core.value(capacity_var);
                for (InfRational pulse : pulses) {
                    Collection<Atom> start_atms = starting_atoms.get(pulse);
                    if (start_atms != null) {
                        overlapping_atoms.addAll(start_atms);
                    }
                    Collection<Atom> end_atms = ending_atoms.get(pulse);
                    if (end_atms != null) {
                        overlapping_atoms.removeAll(end_atms);
                    }
                    InfRational c_use = new InfRational();
                    for (Atom atm : overlapping_atoms) {
                        Item.ArithItem use_amnt = atm.get(REUSABLE_RESOURCE_AMOUNT);
                        c_use.add(core.value(use_amnt));
                    }
                    if (c_use.gt(capacity_val)) {
                        // we have a peak..
                        peaks.add(new RRFlaw((Solver) core, overlapping_atoms.toArray(new Atom[overlapping_atoms.size()])));
                    }
                }
            }

            return peaks;
        }
    }

    private static class RRFlaw extends Flaw {

        private final Atom[] overlapping_atoms;

        private RRFlaw(Solver slv, Atom[] overlapping_atoms) {
            super(slv, Stream.of(overlapping_atoms).flatMap(atom -> slv.getReason(atom).getResolvers().stream()).filter(res -> (res instanceof SupportFlaw.ActivateFact) || (res instanceof SupportFlaw.ActivateGoal)).collect(Collectors.toList()));
            this.overlapping_atoms = overlapping_atoms;
        }

        @Override
        protected void compute_resolvers() throws CoreException {
            for (Atom[] atms : new CombinationGenerator<>(2, overlapping_atoms)) {
                Item.ArithItem a0_start = atms[0].get("start");
                Item.ArithItem a0_end = atms[0].get("end");
                Item.ArithItem a1_start = atms[1].get("start");
                Item.ArithItem a1_end = atms[1].get("end");

                add_resolver(new OrderResolver(slv, slv.leq(a0_end, a1_start).l.v, this, atms[0], atms[1]));
                add_resolver(new OrderResolver(slv, slv.leq(a1_end, a0_start).l.v, this, atms[1], atms[0]));

                Item a0_tau = atms[0].get(TAU);
                if (a0_tau instanceof Item.VarItem) {
                    Set<IVarVal> vals = slv.value((Item.VarItem) a0_tau);
                    if (vals.size() > 1) {
                        for (IVarVal val : vals) {
                            add_resolver(new DisplaceResolver(slv, this, atms[0], (Item.VarItem) a0_tau, (Item) val));
                        }
                    }
                }
                Item a1_tau = atms[1].get(TAU);
                if (a1_tau instanceof Item.VarItem) {
                    Set<IVarVal> vals = slv.value((Item.VarItem) a1_tau);
                    if (vals.size() > 1) {
                        for (IVarVal val : vals) {
                            add_resolver(new DisplaceResolver(slv, this, atms[1], (Item.VarItem) a1_tau, (Item) val));
                        }
                    }
                }
            }
        }

        @Override
        public String getLabel() {
            return "φ" + getPhi() + " rr-flaw";
        }

        @Override
        public String toString() {
            return "<html>φ" + getPhi() + " sv-flaw<br>" + Arrays.stream(overlapping_atoms).map(atm -> atm.toString()).collect(Collectors.joining("<br>")) + "</html>";
        }
    }

    private static class OrderResolver extends Resolver {

        private final Atom before_atm;
        private final Atom after_atm;

        private OrderResolver(final Solver slv, final int rho, final RRFlaw effect, final Atom before, final Atom after) {
            super(slv, rho, new Rational(), effect);
            this.before_atm = before;
            this.after_atm = after;
        }

        @Override
        protected void expand() throws CoreException {
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " σ" + before_atm.sigma + " <= σ" + after_atm.sigma;
        }
    }

    private static class DisplaceResolver extends Resolver {

        private final Atom atom;
        private final Item.VarItem tau;
        private final Item rr;

        private DisplaceResolver(final Solver slv, final RRFlaw effect, final Atom atom, final Item.VarItem tau, final Item rr) {
            super(slv, new Rational(), effect);
            this.atom = atom;
            this.tau = tau;
            this.rr = rr;
        }

        @Override
        protected void expand() throws CoreException {
            if (!slv.sat_core.newClause(new Lit(rho, false), new Lit(slv.var_theory.allows(tau.var, rr), false)));
        }

        @Override
        public String getLabel() {
            return "ρ" + rho + " displ (τ" + tau.var + ") != " + rr;
        }
    }
}
