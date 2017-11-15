package edu.colorodo.clear.wsd.classifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Sparse vector builder.
 *
 * @author jamesgung
 */
public class SparseVectorBuilder {

    private List<Integer> indices = new ArrayList<>();
    private List<Float> values = new ArrayList<>();

    /**
     * Add a new value to this sparse vector.
     *
     * @param index index of value
     * @param value value
     * @return this {@link SparseVectorBuilder}
     */
    public SparseVectorBuilder addValue(int index, float value) {
        indices.add(index);
        values.add(value);
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
        int[] indexArray = indices.stream().mapToInt(i -> i).toArray();
        float[] valueArray = new float[indexArray.length];
        int index = 0;
        for (float val : values) {
            valueArray[index++] = val;
        }
        return new DefaultSparseVector(indexArray, valueArray);
    }

}
