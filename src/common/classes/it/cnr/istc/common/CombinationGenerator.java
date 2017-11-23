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
package it.cnr.istc.common;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 *
 * @author Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 */
public class CombinationGenerator<T> implements Iterable<T[]> {

    private final T[] elements;
    private final int[] combinationIndices;
    private final int size;

    @SuppressWarnings("unchecked")
    public CombinationGenerator(int length, T... elements) {
        assert length > 0;
        assert elements.length > 0;
        this.elements = elements;
        this.combinationIndices = new int[length];
        this.size = MathUtils.combinations(elements.length, length);
    }

    public long getSize() {
        return size;
    }

    @Override
    public Iterator<T[]> iterator() {
        return new Iterator<T[]>() {
            private int cursor = 0;

            {
                for (int i = 0; i < combinationIndices.length; i++) {
                    combinationIndices[i] = i;
                }
            }

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            /**
             * Algorithm from Kenneth H. Rosen, Discrete Mathematics and Its
             * Applications, 2nd edition (NY: McGraw-Hill, 1991), p. 286.
             */
            @Override
            public T[] next() {
                @SuppressWarnings("unchecked")
                T[] ret = (T[]) Array.newInstance(elements.getClass().getComponentType(), combinationIndices.length);
                for (int j = 0; j < combinationIndices.length; j++) {
                    ret[j] = elements[combinationIndices[j]];
                }
                if (++cursor < size) {
                    int i = combinationIndices.length - 1;
                    while (combinationIndices[i] == elements.length - combinationIndices.length + i) {
                        i--;
                    }
                    combinationIndices[i]++;
                    for (int j = i + 1; j < combinationIndices.length; j++) {
                        combinationIndices[j] = combinationIndices[i] + j - i;
                    }
                }
                return ret;
            }
        };
    }
}
