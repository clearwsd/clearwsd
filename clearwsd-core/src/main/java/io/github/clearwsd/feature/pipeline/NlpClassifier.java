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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.DefaultSparseInstance;
import io.github.clearwsd.classifier.DefaultSparseVector;
import io.github.clearwsd.classifier.DummyClassifier;
import io.github.clearwsd.classifier.Hyperparameter;
import io.github.clearwsd.classifier.SparseClassifier;
import io.github.clearwsd.classifier.SparseInstance;
import io.github.clearwsd.feature.model.BaseFeatureModel;
import io.github.clearwsd.feature.model.BaseVocabulary;
import io.github.clearwsd.feature.model.FeatureModel;
import io.github.clearwsd.feature.util.VocabularyBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * NLP classifier that includes a {@link FeaturePipeline} used to convert
 * each input to a {@link SparseInstance} for input to a {@link SparseClassifier}.
 * If training data only contains one label, trains a dummy classifier that always predicts
 * that label rather than going through the full training process.
 *
 * @param <U> input type
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class NlpClassifier<U extends NlpInstance> implements Classifier<U, String> {

    private static final long serialVersionUID = -7706241433014645184L;

    private SparseClassifier sparseClassifier;
    private FeaturePipeline<U> featurePipeline;

    public NlpClassifier(SparseClassifier sparseClassifier, FeaturePipeline<U> featurePipeline) {
        this.sparseClassifier = sparseClassifier;
        this.featurePipeline = featurePipeline;
    }

    @Override
    public String classify(U instance) {
        return featurePipeline.model().label(sparseClassifier.classify(featurePipeline.process(instance)));
    }

    @Override
    public Map<String, Double> score(U instance) {
        FeatureModel model = featurePipeline.model();
        return sparseClassifier.score(featurePipeline.process(instance)).entrySet().stream()
                .collect(Collectors.toMap(e -> model.label(e.getKey()), Map.Entry::getValue));
    }

    @Override
    public void train(List<U> train, List<U> valid) {
        List<SparseInstance> trainInstances = featurePipeline.train(train);
        List<SparseInstance> validInstances = valid.stream()
                .map(featurePipeline::process)
                .collect(Collectors.toList());
        if (featurePipeline.model().labels().indices().size() >= 2) {
            sparseClassifier.train(trainInstances, validInstances);
        } else {
            featurePipeline = new DummyPipeline<>(featurePipeline.model().labels().value(0));
            sparseClassifier = new DummyClassifier(0);
        }
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return sparseClassifier.hyperparameters();
    }

    @Override
    public void initialize(Properties properties) {
        sparseClassifier.initialize(properties);
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            sparseClassifier = (SparseClassifier) inputStream.readObject();
            //noinspection unchecked
            featurePipeline = (FeaturePipeline<U>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(sparseClassifier);
            outputStream.writeObject(featurePipeline);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DummyPipeline<I extends NlpInstance> implements FeaturePipeline<I> {

        private static final long serialVersionUID = -1320433214836264964L;

        private FeatureModel model;

        DummyPipeline(String label) {
            VocabularyBuilder builder = new VocabularyBuilder();
            builder.index(label);
            this.model = new BaseFeatureModel(builder.build(), new BaseVocabulary(new HashMap<>()));
        }

        @Override
        public FeatureModel model() {
            return model;
        }

        @Override
        public SparseInstance process(I inputInstance) {
            return new DefaultSparseInstance(0, 0, new DefaultSparseVector(new int[]{0}, new float[]{1}));
        }

        @Override
        public List<SparseInstance> train(List<I> instances) {
            throw new UnsupportedOperationException();
        }

    }

}
