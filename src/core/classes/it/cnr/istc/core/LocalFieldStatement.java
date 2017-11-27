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

import java.util.List;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
class LocalFieldStatement extends Statement {

    private final List<String> field_type;
    private final String name;
    private final Expression xpr;

    LocalFieldStatement(final List<String> ids, final String n, final Expression e) {
        this.field_type = ids;
        this.name = n;
        this.xpr = e;
    }

    @Override
    public void execute(IScope scp, IEnv env) throws UnsolvableException {
        IScope sc = scp;
        for (String id : field_type) {
            sc = sc.getType(id);
        }
        Type tp = (Type) sc;
        if (env instanceof Core) {
            ((Core) env).items.put(name, tp.primitive ? tp.newInstance(env) : tp.newExistential());
        } else {
            ((Env) env).items.put(name, tp.primitive ? tp.newInstance(env) : tp.newExistential());
        }
    }
}
