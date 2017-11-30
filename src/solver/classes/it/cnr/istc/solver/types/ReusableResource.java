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

import it.cnr.istc.core.Atom;
import it.cnr.istc.core.CoreException;
import static it.cnr.istc.core.IScope.TAU;
import it.cnr.istc.core.Item;
import it.cnr.istc.core.Predicate;
import static it.cnr.istc.smt.LBool.True;
import it.cnr.istc.smt.lra.InfRational;
import it.cnr.istc.smt.var.IVarVal;
import it.cnr.istc.solver.Flaw;
import it.cnr.istc.solver.SmartType;
import it.cnr.istc.solver.Solver;
import it.cnr.istc.solver.SupportFlaw;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class ReusableResource extends SmartType {

    public static final String REUSABLE_RESOURCE = "ReusableResource";
    public static final String REUSABLE_RESOURCE_CAPACITY = "Capacity";
    public static final String REUSABLE_RESOURCE_USE = "Use";
    private final Map<Atom, RRAtomListener> atoms = new IdentityHashMap<>();
    private Collection<Item> to_check = new HashSet<>();

    public ReusableResource(final Solver slv) {
        super(slv, slv, REUSABLE_RESOURCE);
    }

    @Override
    protected void newPredicate(Predicate p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Flaw> getFlaws() {
        if (to_check.isEmpty()) {
            // nothing has changed since last inconsistency check..
            return Collections.emptyList();
        } else {
            // we collect atoms for each state variable..
            Map<Item, Collection<Atom>> rr_atoms = new IdentityHashMap<>();
            for (Map.Entry<Atom, RRAtomListener> atom : atoms.entrySet()) {
                // we filter out those which are not strictly active..
                if (core.sat_core.value(atom.getKey().sigma) == True) {
                    Item tau = atom.getKey().get(TAU);
                    if (tau instanceof Item.VarItem) {
                        for (IVarVal val : core.var_theory.value(((Item.VarItem) tau).var)) {
                            Item c_val = (Item) val;
                            Collection<Atom> atms = rr_atoms.get(c_val);
                            if (atms == null) {
                                atms = new ArrayList<>();
                                rr_atoms.put(c_val, atms);
                            }
                            atms.add(atom.getKey());
                        }
                    } else {
                        Collection<Atom> atms = rr_atoms.get(tau);
                        if (atms == null) {
                            atms = new ArrayList<>();
                            rr_atoms.put(tau, atms);
                        }
                        atms.add(atom.getKey());
                    }
                }
            }

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
                        ending_atoms.put(start_val, end_atms);
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
                        Item.ArithItem use_amnt = atm.get(REUSABLE_RESOURCE_USE);
                        c_use.add(core.value(use_amnt));
                    }
                    if (c_use.gt(capacity_val)) {
                        // we have a peak..
                        peaks.add(new RRFlaw((Solver) core, overlapping_atoms));
                    }
                }
            }

            return peaks;
        }
    }

    private static class RRFlaw extends Flaw {

        private RRFlaw(Solver slv, Collection<Atom> overlapping_atoms) {
            super(slv, overlapping_atoms.stream().flatMap(atom -> slv.getReason(atom).getResolvers().stream()).filter(res -> (res instanceof SupportFlaw.ActivateFact) || (res instanceof SupportFlaw.ActivateGoal)).collect(Collectors.toList()));
        }

        @Override
        protected void compute_resolvers() throws CoreException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLabel() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static class RRAtomListener extends AtomListener {

        private final ReusableResource rr;

        private RRAtomListener(Atom atom, final ReusableResource rr) {
            super(atom);
            this.rr = rr;
        }

        private void something_changed() {
            Item tau = atom.get(TAU);
            if (tau instanceof Item.VarItem) {
                rr.to_check.addAll(rr.getCore().var_theory.value(((Item.VarItem) tau).var).stream().map(var_val -> (Item) var_val).collect(Collectors.toList()));
            } else {
                rr.to_check.add(tau);
            }
        }

        @Override
        public void satValueChange(int v) {
            something_changed();
        }

        @Override
        public void lraValueChange(int v) {
            something_changed();
        }

        @Override
        public void varValueChange(int v) {
            something_changed();
        }
    }
}
