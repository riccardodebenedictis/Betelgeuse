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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class Env implements IEnv {

    protected final Core core;
    protected final IEnv env;
    final Map<String, Item> items = new HashMap<>();

    Env(final Core core, final IEnv env) {
        this.core = core;
        this.env = env;
    }

    @Override
    public Core getCore() {
        return core;
    }

    @Override
    public IEnv getEnv() {
        return env;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Item> T get(final String name) {
        T it = (T) items.get(name);
        if (it != null) {
            return it;
        }
        // if not here, check any enclosing environment
        return env.get(name);
    }

    @Override
    public String toString() {
        return items.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining(", "));
    }
}
