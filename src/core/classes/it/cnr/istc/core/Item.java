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

import it.cnr.istc.smt.lra.Lin;
import static it.cnr.istc.core.Type.BOOL;
import it.cnr.istc.smt.LBool;
import static it.cnr.istc.smt.LBool.Undefined;
import it.cnr.istc.smt.Lit;
import static it.cnr.istc.smt.SatCore.FALSE_var;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import it.cnr.istc.smt.var.IVarVal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Item extends Env implements IVarVal {

    public final Type type;

    Item(final Core core, final IEnv env, final Type type) {
        super(core, env);
        this.type = type;
    }

    public int eq(final Item i) {
        if (this == i) {
            return TRUE_var;
        } else if (!type.name.equals(i.type.name)) {
            return FALSE_var;
        } else if (i instanceof VarItem) {
            return i.eq(this);
        } else {
            Collection<Lit> eqs = new ArrayList<>();
            Queue<Type> q = new ArrayDeque<>();
            q.add(type);
            while (!q.isEmpty()) {
                Type tp = q.poll();
                for (Map.Entry<String, Field> fld : tp.getFields().entrySet()) {
                    if (!fld.getValue().synthetic) {
                        eqs.add(new Lit(items.get(fld.getKey()).eq(i.items.get(fld.getKey()))));
                    }
                }
                q.addAll(tp.getSupertypes());
            }
            switch (eqs.size()) {
                case 0:
                    return TRUE_var;
                case 1:
                    return eqs.iterator().next().v;
                default:
                    return core.sat_core.newConj(eqs.toArray(new Lit[eqs.size()]));
            }
        }
    }

    public boolean equates(final Item i) {
        if (this == i) {
            return true;
        } else if (!type.name.equals(i.type.name)) {
            return false;
        } else if (i instanceof VarItem) {
            return i.equates(this);
        } else {
            Queue<Type> q = new ArrayDeque<>();
            q.add(type);
            while (!q.isEmpty()) {
                Type tp = q.poll();
                for (Map.Entry<String, Field> fld : tp.getFields().entrySet()) {
                    if (!fld.getValue().synthetic && !items.get(fld.getKey()).equates(i.items.get(fld.getKey()))) {
                        return false;
                    }
                }
                q.addAll(tp.getSupertypes());
            }
            return true;
        }
    }

    public static class BoolItem extends Item {

        public final Lit l;

        BoolItem(final Core core, final Lit l) {
            super(core, core, core.getType(BOOL));
            this.l = l;
        }

        @Override
        public int eq(Item i) {
            if (this == i) {
                return TRUE_var;
            } else if (i instanceof BoolItem) {
                return core.sat_core.newEq(l, ((BoolItem) i).l);
            } else {
                return FALSE_var;
            }
        }

        @Override
        public boolean equates(Item i) {
            if (this == i) {
                return true;
            } else if (i instanceof BoolItem) {
                LBool c_val = core.sat_core.value(l);
                LBool i_val = core.sat_core.value(((BoolItem) i).l);
                return c_val == i_val || c_val == Undefined || i_val == Undefined;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return l + ": " + core.sat_core.value(l);
        }
    }

    public static class ArithItem extends Item {

        public final Lin l;

        ArithItem(final Core core, final Type type, final Lin l) {
            super(core, core, type);
            this.l = l;
        }

        @Override
        public int eq(Item i) {
            if (this == i) {
                return TRUE_var;
            } else if (i instanceof ArithItem) {
                return core.la_theory.newEq(l, ((ArithItem) i).l);
            } else {
                return FALSE_var;
            }
        }

        @Override
        public boolean equates(Item i) {
            if (this == i) {
                return true;
            } else if (i instanceof ArithItem) {
                return core.la_theory.ub(l).geq(core.la_theory.lb(((ArithItem) i).l)) && core.la_theory.lb(l).leq(core.la_theory.ub(((ArithItem) i).l)); // the two intervals intersect..
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return l + ": " + core.la_theory.value(l) + " [" + core.la_theory.lb(l) + ", " + core.la_theory.ub(l) + "]";
        }
    }

    public static class StringItem extends Item {

        String val;

        StringItem(final Core core, final String val) {
            super(core, core, core.getType(BOOL));
            this.val = val;
        }

        public String getVal() {
            return val;
        }

        public void setVal(final String val) {
            this.val = val;
        }

        @Override
        public int eq(Item i) {
            if (this == i) {
                return TRUE_var;
            } else if (i instanceof StringItem) {
                return val.equals(((StringItem) i).val) ? TRUE_var : FALSE_var;
            } else {
                return FALSE_var;
            }
        }

        @Override
        public boolean equates(Item i) {
            if (this == i) {
                return true;
            } else if (i instanceof StringItem) {
                return val.equals(((StringItem) i).val);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public static class VarItem extends Item {

        public final int var;

        VarItem(final Core core, final Type type, final int var) {
            super(core, core, type);
            this.var = var;
        }

        @Override
        public <T extends Item> T get(String name) {
            if (!type.getFields().containsKey(name)) {
                return super.get(name);
            } else {
                Item itm = items.get(name);
                if (itm == null) {
                    Set<IVarVal> vals = core.var_theory.value(var);
                    assert !vals.isEmpty();
                    if (vals.size() == 1) {
                        return (T) vals.iterator().next();
                    }
                    int[] c_vars = new int[vals.size()];
                    IVarVal[] c_vals = new IVarVal[vals.size()];
                    int i = 0;
                    for (IVarVal val : vals) {
                        c_vars[i] = core.var_theory.allows(var, val);
                        c_vals[i] = ((Item) val).get(name);
                        i++;
                    }
                    itm = new VarItem(core, type, core.var_theory.newVar(c_vars, c_vals));
                    items.put(name, itm);
                }
                return (T) itm;
            }
        }

        @Override
        public int eq(Item i) {
            if (this == i) {
                return TRUE_var;
            } else if (i instanceof VarItem) {
                return core.var_theory.newEq(var, ((VarItem) i).var);
            } else {
                return core.var_theory.allows(var, i);
            }
        }

        @Override
        public boolean equates(Item i) {
            if (this == i) {
                return true;
            } else if (i instanceof VarItem) {
                Set<IVarVal> l_vals = core.var_theory.value(var);
                Set<IVarVal> r_vals = core.var_theory.value(((VarItem) i).var);
                if (l_vals.size() < r_vals.size()) {
                    for (IVarVal l_val : l_vals) {
                        if (r_vals.contains(l_val)) {
                            return true;
                        }
                    }
                } else {
                    for (IVarVal r_val : r_vals) {
                        if (l_vals.contains(r_val)) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                return core.var_theory.value(var).contains(i);
            }
        }
    }
}
