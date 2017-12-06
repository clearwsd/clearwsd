package edu.colorado.clear.wsd.classifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * Multi-model classifier. Given a key function, map inputs onto sub-models, specialized for the input types.
 *
 * @author jamesgung
 */
@Slf4j
public class MultiClassifier<U, V> implements Classifier<U, V> {

    private static final long serialVersionUID = 2665985487749568860L;

    private Function<U, String> keyFunction;
    private transient Supplier<Classifier<U, V>> prototypeClassifier;
    private Map<String, Classifier<U, V>> classifierMap;

    /**
     * Instantiate a multi-model classifier with a function used to determine which sub-model to apply to a given istanceinstance.
     *
     * @param keyFunction         function mapping input instances onto keys/models
     * @param prototypeClassifier base classifier model
     */
    public MultiClassifier(Function<U, String> keyFunction, Supplier<Classifier<U, V>> prototypeClassifier) {
        this.keyFunction = keyFunction;
        this.prototypeClassifier = prototypeClassifier;
        classifierMap = new HashMap<>();
    }

    @Override
    public V classify(U instance) {
        String key = keyFunction.apply(instance);
        if (classifierMap.containsKey(key)) {
            classifierMap.get(key).classify(instance);
        }
        return null;
    }

    @Override
    public Map<V, Double> score(U instance) {
        String key = keyFunction.apply(instance);
        if (classifierMap.containsKey(key)) {
            return classifierMap.get(key).score(instance);
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public void train(List<U> train, List<U> valid) {
        ImmutableListMultimap<String, U> trainSplits = Multimaps.index(train, keyFunction::apply);
        ImmutableListMultimap<String, U> validSplits = Multimaps.index(valid, keyFunction::apply);
        int numCategories = trainSplits.keySet().size();
        int index = 1;
        for (String category : trainSplits.keySet()) {
            log.debug("Training model for {} ({} of {})", category, index++, numCategories);
            ImmutableList<U> trainCat = trainSplits.get(category);
            ImmutableList<U> validCat = validSplits.get(category);
            Classifier<U, V> classifier = prototypeClassifier.get();
            classifier.train(trainCat, validCat);
            classifierMap.put(category, classifier);
        }
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return prototypeClassifier.get().hyperparameters();
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            //noinspection unchecked
            classifierMap = (Map<String, Classifier<U, V>>) inputStream.readObject();
            //noinspection unchecked
            keyFunction = (Function<U, String>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifierMap);
            outputStream.writeObject(keyFunction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
