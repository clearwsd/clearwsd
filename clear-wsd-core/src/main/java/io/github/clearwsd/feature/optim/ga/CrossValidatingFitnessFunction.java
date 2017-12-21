package io.github.clearwsd.feature.optim.ga;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.function.Function;

import io.github.clearwsd.feature.pipeline.NlpClassifier;
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
