package edu.colorado.clear.wsd.classifier;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

/**
 * Single-label classifier.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class DummyClassifier implements SparseClassifier {

    private static final long serialVersionUID = 4110868818631078034L;
    private int label;

    @Override
    public Integer classify(SparseInstance instance) {
        return label;
    }

    @Override
    public Map<Integer, Double> score(SparseInstance instance) {
        return ImmutableMap.of(label, 1d);
    }

    @Override
    public void train(List<SparseInstance> train, List<SparseInstance> valid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return new ArrayList<>();
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            label = inputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.write(label);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
