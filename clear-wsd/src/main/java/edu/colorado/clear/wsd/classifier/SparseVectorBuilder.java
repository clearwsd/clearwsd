package edu.colorado.clear.wsd.classifier;

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
