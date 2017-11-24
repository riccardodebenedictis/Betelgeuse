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

import it.cnr.istc.common.InfRational;
import it.cnr.istc.common.Lin;
import it.cnr.istc.common.Rational;
import static it.cnr.istc.common.Rational.ZERO;
import it.cnr.istc.core.Item.ArithItem;
import it.cnr.istc.core.Item.BoolItem;
import it.cnr.istc.core.Item.StringItem;
import it.cnr.istc.core.Item.VarItem;
import static it.cnr.istc.core.Type.BOOL;
import static it.cnr.istc.core.Type.INT;
import static it.cnr.istc.core.Type.REAL;
import it.cnr.istc.parser.Parser;
import it.cnr.istc.parser.ParsingException;
import it.cnr.istc.parser.declarations.CompilationUnit;
import it.cnr.istc.smt.LBool;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import static it.cnr.istc.smt.SatCore.FALSE_var;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import it.cnr.istc.smt.lra.LRATheory;
import it.cnr.istc.smt.var.IVarVal;
import it.cnr.istc.smt.var.VarTheory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public abstract class Core implements IScope, IEnv {

    public final SatCore sat_core = new SatCore();
    public final LRATheory la_theory = new LRATheory(sat_core);
    public final VarTheory var_theory = new VarTheory(sat_core);
    final Map<String, Field> fields = new HashMap<>();
    final Map<String, Collection<Method>> methods = new HashMap<>();
    final Map<String, Type> types = new HashMap<>();
    final Map<String, Predicate> predicates = new HashMap<>();
    final Map<String, Item> items = new HashMap<>();
    private final Parser parser = new Parser();
    private int tmp_var = -1;
    private int ctr_var = TRUE_var;

    public Core() {
        newTypes(new Type.BoolType(this), new Type.IntType(this), new Type.RealType(this), new Type.StringType(this));
    }

    protected final void newTypes(final Type... ts) {
        for (Type t : ts) {
            types.put(t.name, t);
        }
    }

    public void read(final String script) throws UnsolvableException, IOException {
        try {
            CompilationUnit cu = parser.parse(new StringReader(script));
            cu.declare(this);
            cu.refine(this);
            cu.execute(this, this);

            if (!sat_core.check()) {
                throw new UnsolvableException("the input problem is inconsistent");
            }
        } catch (ParsingException ex) {
            throw new IOException("parsing exception..", ex);
        }
    }

    public void read(final Reader[] readers) throws UnsolvableException, IOException {
        try {
            CompilationUnit[] cus = new CompilationUnit[readers.length];
            for (int i = 0; i < readers.length; i++) {
                cus[i] = parser.parse(readers[i]);
            }
            for (CompilationUnit cu : cus) {
                cu.declare(this);
            }
            for (CompilationUnit cu : cus) {
                cu.refine(this);
            }
            for (CompilationUnit cu : cus) {
                cu.execute(this, this);
            }

            if (!sat_core.check()) {
                throw new UnsolvableException("the input problem is inconsistent");
            }
        } catch (ParsingException ex) {
            throw new IOException("parsing exception..", ex);
        }
    }

    public BoolItem newBool() {
        return new BoolItem(this, new Lit(sat_core.newVar()));
    }

    public BoolItem newBool(final boolean val) {
        return new BoolItem(this, new Lit(val ? TRUE_var : FALSE_var));
    }

    public ArithItem newInt() {
        return new ArithItem(this, types.get(INT), new Lin(la_theory.newVar(), Rational.ONE));
    }

    public ArithItem newInt(final long val) {
        return new ArithItem(this, types.get(INT), new Lin(new Rational(val)));
    }

    public ArithItem newReal() {
        return new ArithItem(this, types.get(REAL), new Lin(la_theory.newVar(), Rational.ONE));
    }

    public ArithItem newReal(final Rational val) {
        return new ArithItem(this, types.get(REAL), new Lin(new Rational(val)));
    }

    public StringItem newString() {
        return new StringItem(this, "");
    }

    public StringItem newString(final String val) {
        return new StringItem(this, val);
    }

    public Item newEnum(final Type type, final Set<Item> vals) {
        assert !vals.isEmpty();
        assert !type.name.equals(BOOL);
        assert !type.name.equals(INT);
        assert !type.name.equals(REAL);
        if (vals.size() == 1) {
            return vals.iterator().next();
        } else {
            return new Item.VarItem(this, type, var_theory.newVar(vals));
        }
    }

    public Item newEnum(final Type type, final int[] vars, final Item[] vals) {
        switch (type.name) {
            case BOOL: {
                BoolItem bi = newBool();
                boolean nc;
                for (int i = 0; i < vars.length; i++) {
                    nc = sat_core.newClause(new Lit(vars[i], false), new Lit(sat_core.newEq(((BoolItem) vals[i]).l, bi.l)));
                    assert nc;
                }
                return bi;
            }
            case INT: {
                ArithItem ai = newInt();
                boolean nc;
                for (int i = 0; i < vars.length; i++) {
                    nc = sat_core.newClause(new Lit(vars[i], false), new Lit(la_theory.newEq(((ArithItem) vals[i]).l, ai.l)));
                    assert nc;
                }
                return ai;
            }
            case REAL: {
                ArithItem ai = newReal();
                boolean nc;
                for (int i = 0; i < vars.length; i++) {
                    nc = sat_core.newClause(new Lit(vars[i], false), new Lit(la_theory.newEq(((ArithItem) vals[i]).l, ai.l)));
                    assert nc;
                }
                return ai;
            }
            default:
                return new Item.VarItem(this, type, var_theory.newVar(vars, vals));
        }
    }

    public BoolItem negate(final BoolItem bi) {
        return new BoolItem(this, bi.l.not());
    }

    public BoolItem eq(final BoolItem l, final BoolItem r) {
        return new BoolItem(this, new Lit(sat_core.newEq(l.l, r.l)));
    }

    public BoolItem conj(final BoolItem... bis) {
        return new BoolItem(this, new Lit(sat_core.newConj(Stream.of(bis).map(bi -> bi.l).toArray(Lit[]::new))));
    }

    public BoolItem disj(final BoolItem... bis) {
        return new BoolItem(this, new Lit(sat_core.newDisj(Stream.of(bis).map(bi -> bi.l).toArray(Lit[]::new))));
    }

    public BoolItem exct_one(final BoolItem... bis) {
        return new BoolItem(this, new Lit(sat_core.newExctOne(Stream.of(bis).map(bi -> bi.l).toArray(Lit[]::new))));
    }

    public ArithItem add(final ArithItem... ais) {
        Lin l = new Lin();
        boolean is_real = false;
        for (ArithItem ai : ais) {
            l.add(ai.l);
            if (ai.type.name.equals(REAL)) {
                is_real = true;
            }
        }
        return new ArithItem(this, types.get(is_real ? REAL : INT), l);
    }

    public ArithItem sub(final ArithItem... ais) {
        Lin l = new Lin();
        boolean is_real = false;
        for (ArithItem ai : ais) {
            l.sub(ai.l);
            if (ai.type.name.equals(REAL)) {
                is_real = true;
            }
        }
        return new ArithItem(this, types.get(is_real ? REAL : INT), l);
    }

    public ArithItem mult(final ArithItem... ais) {
        Lin l = new Lin();
        boolean is_real = false;
        for (ArithItem ai : ais) {
            assert la_theory.lb(ai.l).eq(la_theory.ub(ai.l)) : "non-linear expression..";
            assert la_theory.value(ai.l).inf.eq(ZERO);
            l.mult(la_theory.value(ai.l).rat);
            if (ai.type.name.equals(REAL)) {
                is_real = true;
            }
        }
        return new ArithItem(this, types.get(is_real ? REAL : INT), l);
    }

    public ArithItem div(final ArithItem... ais) {
        Lin l = new Lin();
        boolean is_real = false;
        for (ArithItem ai : ais) {
            assert la_theory.lb(ai.l).eq(la_theory.ub(ai.l)) : "non-linear expression..";
            assert la_theory.value(ai.l).inf.eq(ZERO);
            l.div(la_theory.value(ai.l).rat);
            if (ai.type.name.equals(REAL)) {
                is_real = true;
            }
        }
        return new ArithItem(this, types.get(is_real ? REAL : INT), l);
    }

    public ArithItem minus(final ArithItem ai) {
        return new ArithItem(this, ai.type, ai.l.minus());
    }

    public BoolItem lt(final ArithItem l, final ArithItem r) {
        return new BoolItem(this, new Lit(la_theory.newLt(l.l, r.l)));
    }

    public BoolItem leq(final ArithItem l, final ArithItem r) {
        return new BoolItem(this, new Lit(la_theory.newLEq(l.l, r.l)));
    }

    public BoolItem eq(final ArithItem l, final ArithItem r) {
        return new BoolItem(this, new Lit(la_theory.newEq(l.l, r.l)));
    }

    public BoolItem geq(final ArithItem l, final ArithItem r) {
        return new BoolItem(this, new Lit(la_theory.newGEq(l.l, r.l)));
    }

    public BoolItem gt(final ArithItem l, final ArithItem r) {
        return new BoolItem(this, new Lit(la_theory.newGt(l.l, r.l)));
    }

    public BoolItem eq(final Item l, final Item r) {
        return new BoolItem(this, new Lit(l.eq(r)));
    }

    public LBool value(final BoolItem bi) {
        return sat_core.value(bi.l);
    }

    public InfRational lb(final ArithItem ai) {
        return la_theory.lb(ai.l);
    }

    public InfRational ub(final ArithItem ai) {
        return la_theory.ub(ai.l);
    }

    public InfRational value(final ArithItem ai) {
        return la_theory.value(ai.l);
    }

    public Set<IVarVal> value(final VarItem vi) {
        return var_theory.value(vi.var);
    }

    protected void setVar(final int v) {
        tmp_var = ctr_var;
        ctr_var = v;
    }

    protected void restoreVar() {
        ctr_var = tmp_var;
    }

    protected abstract void newFact(final Atom atom);

    protected abstract void newGoal(final Atom atom);

    protected abstract void newDisjunction(final IEnv env, final Disjunction dsj);

    @Override
    public Core getCore() {
        return this;
    }

    @Override
    public IScope getScope() {
        return null;
    }

    @Override
    public Field getField(String name) {
        Field f = fields.get(name);
        if (f != null) {
            return f;
        }
        throw new NoSuchFieldError(name);
    }

    @Override
    public Map<String, Field> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public Method getMethod(String name, Type... pars) {
        Collection<Method> c_ms = methods.get(name);
        if (c_ms != null) {
            for (Method mthd : c_ms) {
                if (mthd.args.length == pars.length) {
                    boolean found = true;
                    for (int i = 0; i < pars.length; i++) {
                        if (!mthd.args[i].type.isAssignableFrom(pars[i])) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        return mthd;
                    }
                }
            }
        }
        throw new NoSuchMethodError(name);
    }

    @Override
    public Type getType(String name) {
        Type tp = types.get(name);
        if (tp != null) {
            return tp;
        }
        throw new NoClassDefFoundError(name);
    }

    @Override
    public Predicate getPredicate(String name) {
        Predicate p = predicates.get(name);
        if (p != null) {
            return p;
        }
        throw new NoClassDefFoundError(name);
    }

    @Override
    public IEnv getEnv() {
        return null;
    }

    @Override
    public <T extends Item> T get(String name) {
        T it = (T) items.get(name);
        if (it != null) {
            return it;
        }
        throw new NoSuchFieldError(name);
    }
}
