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

package io.github.clearwsd.feature.optim.ga;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.function.Function;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.eval.CrossValidation;
import io.github.clearwsd.eval.CrossValidation.Fold;
import io.github.clearwsd.eval.Evaluation;
import io.github.clearwsd.feature.pipeline.NlpClassifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Default fitness function--k-fold cross-validation is computed to determine the fitness value.
 *
 * @author jamesgung
 */
@Slf4j
@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor
public class CrossValidatingFitnessFunction<T extends NlpInstance> implements Function<NlpClassifier<T>, Double> {

    private List<Fold<T>> folds;
    private CrossValidation<T> cv;

    private int numFolds = 5;
    private double samplingRatio = 0.8;

    public CrossValidatingFitnessFunction(CrossValidation<T> cv) {
        this.cv = cv;
    }

    public void initialize(List<T> instances) {
        folds = cv.createFolds(instances, numFolds, samplingRatio);
    }

    @Override
    public Double apply(NlpClassifier<T> genotype) {
        Preconditions.checkState(folds != null, "Must initialize with data before applying function.");
        Evaluation evaluations = new Evaluation(cv.crossValidate(genotype, folds));
        return evaluations.f1();
    }

}
