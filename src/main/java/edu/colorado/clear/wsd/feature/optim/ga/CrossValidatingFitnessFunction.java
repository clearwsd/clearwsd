package edu.colorado.clear.wsd.feature.optim.ga;

import java.util.List;
import java.util.function.Function;

import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.CrossValidation.Fold;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;

/**
 * Default fitness function--k-fold cross-validation is computed to determine the fitness value.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class CrossValidatingFitnessFunction<T extends NlpInstance> implements Function<NlpClassifier<T>, Double> {

    private List<Fold<T>> folds;
    private CrossValidation<T> cv;

    @Override
    public Double apply(NlpClassifier<T> genotype) {
        Evaluation evaluations = new Evaluation(cv.crossValidate(genotype, folds));
        return evaluations.f1();
    }

}
