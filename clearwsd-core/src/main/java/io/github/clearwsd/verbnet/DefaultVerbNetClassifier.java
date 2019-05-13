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

package io.github.clearwsd.verbnet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.Hyperparameter;
import io.github.clearwsd.classifier.PaClassifier;
import io.github.clearwsd.classifier.SparseClassifier;
import io.github.clearwsd.feature.pipeline.AnnotatingClassifier;
import io.github.clearwsd.feature.pipeline.NlpClassifier;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.verbnet.VerbNetFeatureUtils.defaultAnnotator;
import static io.github.clearwsd.verbnet.VerbNetFeatureUtils.defaultFeatures;
import static io.github.clearwsd.verbnet.VerbNetFeatureUtils.defaultResources;
import static io.github.clearwsd.verbnet.VerbNetFeatureUtils.sharedFeatures;

/**
 * Default VerbNet classifier.
 *
 * @author jamesgung
 */
@Slf4j
public class DefaultVerbNetClassifier implements Classifier<NlpFocus<DepNode, DepTree>, String> {

    private static final long serialVersionUID = -3815702452161005214L;

    private AnnotatingClassifier<NlpFocus<DepNode, DepTree>> classifier;
    private FeatureResourceManager resources;

    public DefaultVerbNetClassifier() {
        resources = defaultResources();
        resources.initialize();
        classifier = initialize();
        classifier.initialize(resources);
    }

    private AnnotatingClassifier<NlpFocus<DepNode, DepTree>> initialize() {
        LabelSharingMultiClassifier<NlpFocus<DepNode, DepTree>> multiClassifier
                = new LabelSharingMultiClassifier<>((Serializable & Function<NlpFocus<DepNode, DepTree>, String>)
                (i) -> i.focus().feature(FeatureType.Predicate),
                () -> new NlpClassifier<>(initializeClassifier(), defaultFeatures()),
                () -> new NlpClassifier<>(initializeClassifier(), sharedFeatures()));
        return new AnnotatingClassifier<>(multiClassifier, defaultAnnotator());
    }

    @Override
    public String classify(NlpFocus<DepNode, DepTree> instance) {
        return classifier.classify(instance);
    }

    @Override
    public Map<String, Double> score(NlpFocus<DepNode, DepTree> instance) {
        return classifier.score(instance);
    }

    @Override
    public void train(List<NlpFocus<DepNode, DepTree>> train, List<NlpFocus<DepNode, DepTree>> valid) {
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
            classifier = (AnnotatingClassifier<NlpFocus<DepNode, DepTree>>) inputStream.readObject();
            resources = (FeatureResourceManager) inputStream.readObject();
            resources.initialize();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
            outputStream.writeObject(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SparseClassifier initializeClassifier() {
        return new PaClassifier();
    }

}
