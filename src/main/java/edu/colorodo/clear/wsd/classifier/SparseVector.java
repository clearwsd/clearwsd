package edu.colorodo.clear.wsd.classifier;

/**
 * A compact representation for one-dimensional arrays where most of the values are zero. Contains indices and associated values for
 * every non-zero value.
 *
 * @author jamesgung
 */
public interface SparseVector {

    /**
     * Indices corresponding to each value in the vector, in ascending order and with no duplicates.
     */
    int[] indices();

    /**
     * Sparse vector of non-zero values.
     */
    float[] data();

}
