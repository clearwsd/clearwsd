package io.github.clearwsd.classifier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Vector instance.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class DefaultSparseInstance implements SparseInstance {

    private static final long serialVersionUID = 8110247727099551710L;

    private int id;

    private int target;

    private SparseVector sparseVector;

    @Override
    public int[] indices() {
        return sparseVector.indices();
    }

    @Override
    public float[] data() {
        return sparseVector.data();
    }

    @Override
    public float l2() {
        return sparseVector.l2();
    }

}
