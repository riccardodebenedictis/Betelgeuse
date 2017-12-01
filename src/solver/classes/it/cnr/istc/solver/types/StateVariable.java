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
import it.cnr.istc.core.AtomListener;
import it.cnr.istc.core.CoreException;
import it.cnr.istc.core.Field;
import static it.cnr.istc.core.IScope.TAU;
import it.cnr.istc.core.Item;
import it.cnr.istc.core.Predicate;
import it.cnr.istc.core.Type;
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
public class StateVariable extends SmartType {

    public static final String STATE_VARIABLE = "StateVariable";
    private final Collection<Atom> atoms = new ArrayList<>();
    private Collection<Item> to_check = new HashSet<>();

    public StateVariable(final Solver slv) {
        super(slv, slv, STATE_VARIABLE);
    }

    @Override
    protected void newPredicate(Predicate p) {
        Type.newSupertypes(p, core.getPredicate("IntervalPredicate"));
        newFields(p, new Field(this, TAU));
    }

    @Override
    protected void newFact(SupportFlaw f) throws CoreException {
        // we apply interval-predicate if the fact becomes active..
        setVar(f.atom.sigma);
        core.getPredicate("IntervalPredicate").applyRule(f.atom);
        restoreVar();

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
            Map<Item, Collection<Atom>> sv_atoms = new IdentityHashMap<>();
            for (Atom atom : atoms) {
                // we filter out those which are not strictly active..
                if (core.sat_core.value(atom.sigma) == True) {
                    Item tau = atom.get(TAU);
                    if (tau instanceof Item.VarItem) {
                        for (IVarVal val : core.var_theory.value(((Item.VarItem) tau).var)) {
                            Item c_val = (Item) val;
                            Collection<Atom> atms = sv_atoms.get(c_val);
                            if (atms == null) {
                                atms = new ArrayList<>();
                                sv_atoms.put(c_val, atms);
                            }
                            atms.add(atom);
                        }
                    } else {
                        Collection<Atom> atms = sv_atoms.get(tau);
                        if (atms == null) {
                            atms = new ArrayList<>();
                            sv_atoms.put(tau, atms);
                        }
                        atms.add(atom);
                    }
                }
            }

            // we collect the peaks..
            Collection<Flaw> peaks = new ArrayList<>();
            for (Map.Entry<Item, Collection<Atom>> c_atoms : sv_atoms.entrySet()) {
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
                for (InfRational pulse : pulses) {
                    Collection<Atom> start_atms = starting_atoms.get(pulse);
                    if (start_atms != null) {
                        overlapping_atoms.addAll(start_atms);
                    }
                    Collection<Atom> end_atms = ending_atoms.get(pulse);
                    if (end_atms != null) {
                        overlapping_atoms.removeAll(end_atms);
                    }
                    if (overlapping_atoms.size() > 1) {
                        // we have a peak..
                        peaks.add(new SVFlaw((Solver) core, overlapping_atoms));
                    }
                }
            }

            return peaks;
        }
    }

    private static class SVFlaw extends Flaw {

        private SVFlaw(Solver slv, Collection<Atom> overlapping_atoms) {
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
}
