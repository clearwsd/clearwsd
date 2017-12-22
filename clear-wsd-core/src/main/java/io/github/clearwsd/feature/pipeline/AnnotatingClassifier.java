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

package io.github.clearwsd.feature.pipeline;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.Hyperparameter;
import io.github.clearwsd.feature.annotator.Annotator;
import io.github.clearwsd.feature.resource.FeatureResourceManager;

/**
 * Classifier wrapper that applies provided annotations prior to training, classification and scoring.
 *
 * @author jamesgung
 */
public class AnnotatingClassifier<T> implements Classifier<T, String> {

    private static final long serialVersionUID = -6677942446282205271L;

    private Classifier<T, String> classifier;
    private Annotator<T> annotator;

    public AnnotatingClassifier(Classifier<T, String> classifier,
                                Annotator<T> annotator) {
        this.classifier = classifier;
        this.annotator = annotator;
    }

    /**
     * Initialize annotators with resources.
     *
     * @param featureResourceManager resource manager
     */
    public void initialize(FeatureResourceManager featureResourceManager) {
        annotator.initialize(featureResourceManager);
    }

    @Override
    public String classify(T instance) {
        Preconditions.checkState(annotator.initialized(), "Annotator is not initialized.");
        instance = annotator.annotate(instance);
        return classifier.classify(instance);
    }

    @Override
    public Map<String, Double> score(T instance) {
        Preconditions.checkState(annotator.initialized(), "Annotator is not initialized.");
        instance = annotator.annotate(instance);
        return classifier.score(instance);
    }

    @Override
    public void train(List<T> train, List<T> valid) {
        Preconditions.checkState(annotator.initialized(), "Annotator is not initialized.");
        train = train.parallelStream().map(annotator::annotate).collect(Collectors.toList());
        valid = valid.parallelStream().map(annotator::annotate).collect(Collectors.toList());
        classifier.train(train, valid);
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return classifier.hyperparameters();
    }

    @Override
    public void initialize(Properties properties) {
        classifier.initialize(properties);
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            //noinspection unchecked
            classifier = (Classifier<T, String>) inputStream.readObject();
            //noinspection unchecked
            annotator = (Annotator<T>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
            outputStream.writeObject(annotator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
