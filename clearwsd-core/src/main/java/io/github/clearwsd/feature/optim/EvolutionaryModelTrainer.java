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

package io.github.clearwsd.feature.optim;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.Hyperparameter;
import io.github.clearwsd.feature.optim.ga.CrossValidatingFitnessFunction;
import io.github.clearwsd.feature.optim.ga.GeneticAlgorithm;
import io.github.clearwsd.feature.pipeline.NlpClassifier;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Model trainer searches for the optimal feature architecture
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class EvolutionaryModelTrainer<U extends NlpInstance> implements Classifier<U, String> {

    private static final long serialVersionUID = -1828098138845207546L;

    private transient GeneticAlgorithm<NlpClassifier<U>> ga;

    @Getter
    private NlpClassifier<U> classifier;

    public EvolutionaryModelTrainer(GeneticAlgorithm<NlpClassifier<U>> ga) {
        this.ga = ga;
    }

    @Override
    public String classify(U instance) {
        return classifier.classify(instance);
    }

    @Override
    public Map<String, Double> score(U instance) {
        return classifier.score(instance);
    }

    @Override
    public void train(List<U> train, List<U> valid) {
        // find the optimal feature architecture through cross-validation on training data
        ((CrossValidatingFitnessFunction<U>) ga.fitnessFunction()).initialize(train);
        ga.run();
        // train on best genotype w/ full training set
        classifier = ga.best().phenotype();
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
            classifier = (NlpClassifier<U>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
