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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Constructor extends Scope {

    final List<Field> arguments;
    final List<Statement> statements;
    final List<Pair<String, List<Expression>>> init_list;

    public Constructor(final Core core, final IScope scope, final Field... args) {
        this(core, scope, Arrays.asList(args), Collections.emptyList(), Collections.emptyList());
    }

    Constructor(final Core core, final IScope scope, final List<Field> args, final List<Statement> statements, final List<Pair<String, List<Expression>>> init_list) {
        super(core, scope);
        this.arguments = args;
        fields.put(THIS, new Field(((Type) scope), THIS));
        for (Field arg : args) {
            fields.put(arg.name, arg);
        }
        this.statements = statements;
        this.init_list = init_list;
    }

    public Item newInstance(final IEnv env, final Item... args) throws CoreException {
        Item itm = ((Type) scope).newInstance(env);
        invoke(itm, args);
        return itm;
    }

    private void invoke(final Item itm, final Item... args) throws CoreException {
        Env e = new Env(core, itm);
        for (int i = 0; i < arguments.size(); i++) {
            e.items.put(arguments.get(i).name, args[i]);
        }

        // we initialize the supertypes..
        int il_idx = 0;
        for (Type st : ((Type) scope).supertypes) {
            if (il_idx < init_list.size() && init_list.get(il_idx).first.equals(st.name)) {
                // explicit supertype constructor invocation..
                List<Item> c_args = new ArrayList<>(arguments.size());
                List<Type> par_types = new ArrayList<>(arguments.size());
                for (Expression xpr : init_list.get(il_idx).second) {
                    Item arg = xpr.evaluate(this, e);
                    c_args.add(arg);
                    par_types.add(arg.type);
                }
                // we assume that the constructor exists..
                st.getConstructor(par_types.toArray(new Type[par_types.size()])).invoke(itm, c_args.toArray(new Item[c_args.size()]));
                il_idx++;
            } else {
                // implicit supertype (default) constructor invocation..
                // we assume that the default constructor exists..
                st.getConstructor().invoke(itm);
            }
        }

        // we procede with the assignment list..
        for (; il_idx < init_list.size(); il_idx++) {
            assert init_list.get(il_idx).second.size() == 1;
            itm.items.put(init_list.get(il_idx).first, init_list.get(il_idx).second.get(0).evaluate(this, e));
        }

        // we instantiate the uninstantiated fields..
        for (Map.Entry<String, Field> arg : scope.getFields().entrySet()) {
            if (!arg.getValue().synthetic && !itm.items.containsKey(arg.getKey())) {
                // the field is uninstantiated..
                if (arg.getValue().expression != null) {
                    itm.items.put(arg.getKey(), arg.getValue().expression.evaluate(this, e));
                } else {
                    Type tp = arg.getValue().type;
                    if (tp.primitive) {
                        itm.items.put(arg.getKey(), tp.newInstance(e));
                    } else {
                        itm.items.put(arg.getKey(), tp.newExistential());
                    }
                }
            }
        }

        // finally, we execute the constructor body..
        for (Statement stmnt : statements) {
            stmnt.execute(this, e);
        }
    }
}
