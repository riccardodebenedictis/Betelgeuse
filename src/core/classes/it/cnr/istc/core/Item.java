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
import static it.cnr.istc.core.Type.BOOL;
import it.cnr.istc.smt.Lit;
import it.cnr.istc.smt.var.IVarVal;

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

    public static class BoolItem extends Item {

        public final Lit l;

        BoolItem(final Core core, final Lit l) {
            super(core, core, core.getType(BOOL));
            this.l = l;
        }
    }

    public static class ArithItem extends Item {

        public final Lin l;

        ArithItem(final Core core, final Type type, final Lin l) {
            super(core, core, type);
            this.l = l;
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
    }

    public static class VarItem extends Item {

        public final int var;

        VarItem(final Core core, final Type type, final int var) {
            super(core, core, type);
            this.var = var;
        }
    }
}
