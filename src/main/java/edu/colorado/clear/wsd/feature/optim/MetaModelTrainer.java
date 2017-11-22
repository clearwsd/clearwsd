package edu.colorado.clear.wsd.feature.optim;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.ClassifierFactory;
import edu.colorado.clear.wsd.classifier.Hyperparameter;
import edu.colorado.clear.wsd.classifier.SparseClassifier;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.CrossValidation.Fold;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Model trainer searches for the optimal feature architecture
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class MetaModelTrainer<U extends NlpInstance> implements Classifier<U, String> {

    private static final long serialVersionUID = 6414091079202692687L;

    private transient FeaturePipelineFactory<U> featureFactory;
    private transient ClassifierFactory<SparseClassifier> classifierFactory;

    private int seed;
    @Setter
    private int folds = 5;
    @Setter
    private double ratio = 0.8;
    @Setter
    private int iterations = 100;

    private NlpClassifier<U> classifier;

    public MetaModelTrainer(FeaturePipelineFactory<U> featureFactory,
                            ClassifierFactory<SparseClassifier> classifierFactory, int seed) {
        this.featureFactory = featureFactory;
        this.classifierFactory = classifierFactory;
        this.seed = seed;
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
        CrossValidation<U> cv = new CrossValidation<>(seed, t -> t.feature(FeatureType.Gold.name()));
        // search for best feature function using cross-validation
        Evaluation best = new Evaluation();
        FeaturePipeline<U> result = null;
        for (int i = 0; i < iterations; ++i) {
            log.debug("Iteration {} (best score: {})", i, best.f1());
            List<Fold<U>> folds = cv.createFolds(train, this.folds, ratio);
            FeaturePipeline<U> featureFunction = featureFactory.create();
            NlpClassifier<U> classifier = new NlpClassifier<>(classifierFactory.create(), featureFunction);
            Evaluation eval = new Evaluation(cv.crossValidate(classifier, folds));
            if (eval.f1() > best.f1()) {
                best = eval;
                result = featureFunction;
            }
        }
        // train final classifier using the top-scoring feature function
        classifier = new NlpClassifier<>(classifierFactory.create(), result);
        classifier.train(train, valid);
        log.debug("Overall top score: {}\n{}", best.f1(), best.toString());
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
