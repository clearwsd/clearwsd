package edu.colorodo.clear.wsd.classifier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Sparse vector implementation.
 *
 * @author jamesgung
 */
@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
public class DefaultSparseVector implements SparseVector {

    private int[] indices;

    private float[] data;

}
