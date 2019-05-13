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
import java.util.stream.Collectors;

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
     * Instantiate a multi-model classifier with a function used to determine which sub-model to apply to a given instance.
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
            return classifierMap.get(key).classify(instance);
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
        for (String category : trainSplits.keySet().stream().sorted(String::compareTo).collect(Collectors.toList())) {
            ImmutableList<U> trainCat = trainSplits.get(category);
            ImmutableList<U> validCat = validSplits.get(category);
            Classifier<U, V> classifier = prototypeClassifier.get();
            if (numCategories > 1) {
                log.debug("Training model {} of {} for \"{}\"", index++, numCategories, category);
            } else {
                log.debug("Training model for \"{}\"", category);
            }
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
