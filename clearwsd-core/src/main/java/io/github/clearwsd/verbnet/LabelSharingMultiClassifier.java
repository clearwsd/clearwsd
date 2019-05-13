/*
 * Copyright 2019 James Gung
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

package io.github.clearwsd.verbnet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.Hyperparameter;
import io.github.clearwsd.feature.pipeline.NlpClassifier;
import io.github.clearwsd.type.NlpInstance;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Multi-model classifier that can aggregate across multiple different model keys depending on some provided heuristic functions.
 *
 * @author jgung
 */
@Slf4j
public class LabelSharingMultiClassifier<U extends NlpInstance> implements Classifier<U, String> {

    private static final long serialVersionUID = 0L;

    private Function<U, String> keyFunction;
    private transient Supplier<NlpClassifier<U>> separateModelPrototype;
    private transient Supplier<NlpClassifier<U>> combinedModelPrototype;
    private Map<String, NlpClassifier<U>> classifierMap = new HashMap<>();

    /**
     * Instantiate a multi-model classifier with a function used to determine which sub-model to apply to a given instance.
     *
     * @param keyFunction            function mapping input instances onto keys/models
     * @param separateModelPrototype base algorithm for individual-key models
     * @param combinedModelPrototype base algorithm for model combining keys
     */
    public LabelSharingMultiClassifier(@NonNull Function<U, String> keyFunction,
                                       @NonNull Supplier<NlpClassifier<U>> separateModelPrototype,
                                       @NonNull Supplier<NlpClassifier<U>> combinedModelPrototype) {
        this.keyFunction = keyFunction;
        this.separateModelPrototype = separateModelPrototype;
        this.combinedModelPrototype = combinedModelPrototype;
    }

    @Override
    public String classify(@NonNull U instance) {
        String key = keyFunction.apply(instance);
        if (classifierMap.containsKey(key)) {
            return classifierMap.get(key).classify(instance);
        }
        return null;
    }

    @Override
    public Map<String, Double> score(@NonNull U instance) {
        String key = keyFunction.apply(instance);
        if (classifierMap.containsKey(key)) {
            return classifierMap.get(key).score(instance);
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public void train(@NonNull List<U> train, @NonNull List<U> valid) {
        ImmutableListMultimap<String, U> trainSplits = Multimaps.index(train, keyFunction::apply);
        ImmutableListMultimap<String, U> validSplits = Multimaps.index(valid, keyFunction::apply);
        int numCategories = trainSplits.keySet().size();
        Set<String> shareKeys = new HashSet<>();
        int index = 1;
        for (String category : trainSplits.keySet().stream().sorted(String::compareTo).collect(Collectors.toList())) {
            ImmutableList<U> trainCat = trainSplits.get(category);
            ImmutableList<U> validCat = validSplits.get(category);
            NlpClassifier<U> classifier = separateModelPrototype.get();
            if (numCategories > 1) {
                log.debug("Training model {} of {} for \"{}\"", index++, numCategories, category);
            } else {
                log.debug("Training model for \"{}\"", category);
            }
            classifier.train(trainCat, validCat);
            classifierMap.put(category, classifier);

            if (classifier.featurePipeline().model().labels().indices().size() == 1) {
                shareKeys.add(category);
            }
        }

        if (shareKeys.size() > 0) {
            // train a combined model across all verbs with only one class in the training data
            NlpClassifier<U> classifier = combinedModelPrototype.get();
            List<U> filteredTrain = train.stream()
                    .filter(s -> shareKeys.contains(keyFunction.apply(s)))
                    .collect(Collectors.toList());
            List<U> filteredValid = valid.stream()
                    .filter(s -> shareKeys.contains(keyFunction.apply(s)))
                    .collect(Collectors.toList());
            if (filteredTrain.size() > 0) {
                log.info("Training shared model on {} instances and {} classes", filteredTrain.size(), shareKeys.size());
                classifier.train(filteredTrain, filteredValid);
                shareKeys.forEach(key -> classifierMap.put(key, classifier));
            }
        }

    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return separateModelPrototype.get().hyperparameters();
    }

    @Override
    public void load(@NonNull ObjectInputStream inputStream) {
        try {
            //noinspection unchecked
            classifierMap = (Map<String, NlpClassifier<U>>) inputStream.readObject();
            //noinspection unchecked
            keyFunction = (Function<U, String>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(@NonNull ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifierMap);
            outputStream.writeObject(keyFunction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
