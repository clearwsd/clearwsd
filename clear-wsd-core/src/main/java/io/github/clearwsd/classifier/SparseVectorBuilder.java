/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.classifier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sparse vector builder.
 *
 * @author jamesgung
 */
public class SparseVectorBuilder {

    private Map<Integer, Float> indexValueMap = new HashMap<>();

    /**
     * Add a new value to this sparse vector.
     *
     * @param index index of value
     * @param value value
     * @return this {@link SparseVectorBuilder}
     */
    public SparseVectorBuilder addValue(int index, float value) {
        indexValueMap.put(index, value);
        return this;
    }

    /**
     * Shorthand for {@link #addValue(int, float)} when the value is 1.
     *
     * @param index index of value
     * @return this {@link SparseVectorBuilder}
     */
    public SparseVectorBuilder addIndex(int index) {
        return addValue(index, 1);
    }

    /**
     * Build an immutable sparse vector given the current state of this builder.
     *
     * @return sparse vector
     */
    public SparseVector build() {
        List<Map.Entry<Integer, Float>> entries = indexValueMap.entrySet().stream().sorted(
                Comparator.comparingInt(Map.Entry::getKey)).collect(Collectors.toList());
        int[] indexArray = new int[entries.size()];
        float[] valueArray = new float[indexArray.length];
        int index = 0;
        for (Map.Entry<Integer, Float> entry : entries) {
            indexArray[index] = entry.getKey();
            valueArray[index++] = entry.getValue();
        }
        return new DefaultSparseVector(indexArray, valueArray);
    }

}
