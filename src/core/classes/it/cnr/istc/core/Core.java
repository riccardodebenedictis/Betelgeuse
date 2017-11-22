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

import it.cnr.istc.common.Lin;
import it.cnr.istc.common.Rational;
import it.cnr.istc.core.Item.ArithItem;
import it.cnr.istc.core.Item.BoolItem;
import it.cnr.istc.core.Item.StringItem;
import static it.cnr.istc.core.Type.BOOL;
import static it.cnr.istc.core.Type.INT;
import static it.cnr.istc.core.Type.REAL;
import it.cnr.istc.parser.Parser;
import it.cnr.istc.parser.declarations.CompilationUnit;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.SatCore;
import static it.cnr.istc.smt.SatCore.FALSE_var;
import static it.cnr.istc.smt.SatCore.TRUE_var;
import it.cnr.istc.smt.lra.LRATheory;
import it.cnr.istc.smt.var.VarTheory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Core implements IScope, IEnv {

    public final SatCore sat_core = new SatCore();
    public final LRATheory la_theory = new LRATheory(sat_core);
    public final VarTheory var_theory = new VarTheory(sat_core);
    final Map<String, Field> fields = new HashMap<>();
    final Map<String, Collection<Method>> methods = new HashMap<>();
    final Map<String, Type> types = new HashMap<>();
    final Map<String, Predicate> predicates = new HashMap<>();
    final Map<String, Item> items = new HashMap<>();
    private final Parser parser = new Parser();

    public Core() {
        newTypes(new Type.BoolType(this), new Type.IntType(this), new Type.RealType(this), new Type.StringType(this));
    }

    protected final void newTypes(final Type... ts) {
        for (Type t : ts) {
            types.put(t.name, t);
        }
    }

    public void read(final String script) throws UnsolvableException, IOException {
        CompilationUnit cu = parser.parse(new StringReader(script));
        cu.declare(this);
        cu.refine(this);
        cu.execute(this, this);

        if (!sat_core.check()) {
            throw new UnsolvableException("the input problem is inconsistent");
        }
    }

    public void read(final Reader[] readers) throws UnsolvableException, IOException {
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
        throw new UnsupportedOperationException("not supported yet..");
    }

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
