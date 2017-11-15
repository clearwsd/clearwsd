package edu.colorodo.clear.wsd.classifier;

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
public class DefaultStringInstance implements StringInstance {

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

}
