package edu.colorado.clear.wsd.classifier;

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

    private static final long serialVersionUID = -3910126278417663876L;

    private int[] indices;

    private float[] data;

    @Override
    public float l2() {
        float sum = 0;
        for (int i = 0; i < indices.length; ++i) {
            sum += data[i] * data[i];
        }
        return (float) Math.sqrt(sum);
    }
}
