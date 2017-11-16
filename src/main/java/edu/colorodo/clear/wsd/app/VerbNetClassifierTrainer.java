package edu.colorodo.clear.wsd.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.classifier.LibLinearClassifier;
import edu.colorodo.clear.wsd.classifier.SparseClassifier;
import edu.colorodo.clear.wsd.classifier.StringInstance;
import edu.colorodo.clear.wsd.corpus.VerbNetReader;
import edu.colorodo.clear.wsd.eval.CrossValidation;
import edu.colorodo.clear.wsd.eval.Evaluation;
import edu.colorodo.clear.wsd.feature.function.BiasFeatureFunction;
import edu.colorodo.clear.wsd.feature.function.DefaultFeatureFunction;
import edu.colorodo.clear.wsd.feature.function.MultiFeatureFunction;
import edu.colorodo.clear.wsd.feature.context.OffsetContextFactory;
import edu.colorodo.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorodo.clear.wsd.feature.extractor.StringFunctionExtractor;
import edu.colorodo.clear.wsd.feature.extractor.string.LowercaseFunction;
import edu.colorodo.clear.wsd.feature.pipeline.BaseFeaturePipeline;
import edu.colorodo.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorodo.clear.wsd.type.DepNode;
import edu.colorodo.clear.wsd.type.DependencyTree;
import edu.colorodo.clear.wsd.type.FeatureType;
import edu.colorodo.clear.wsd.type.FocusInstance;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static edu.colorodo.clear.wsd.classifier.LibLinearClassifier.LiblinearModel;
import static edu.colorodo.clear.wsd.type.FeatureType.Gold;

/**
 * VerbNet classifier trainer.
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class VerbNetClassifierTrainer {

    @Setter
    private FeaturePipeline<FocusInstance<DepNode, DependencyTree>> features;
    @Setter
    private SparseClassifier<LiblinearModel> classifier;

    public void train(List<FocusInstance<DepNode, DependencyTree>> train, List<FocusInstance<DepNode, DependencyTree>> valid) {
        LiblinearModel model = new LiblinearModel();

        // train features
        features.initialize(model);
        List<StringInstance> trainingInstances = features.train(train);
        List<StringInstance> validationInstances = valid.stream()
                .map(features::process)
                .collect(Collectors.toList());

        // train classifier parameters
        classifier.initialize(model);
        classifier.train(trainingInstances, validationInstances);

        // evaluate model
        Evaluation evaluation = new Evaluation();
        for (StringInstance vector : validationInstances) {
            evaluation.add(classifier.classify(vector), model.label(vector.target()));
        }
        log.info("Validation performance: \n{}", evaluation.toString());
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        List<CrossValidation.Fold<FocusInstance<DepNode, DependencyTree>>> folds
                = new CrossValidation<>((FocusInstance<DepNode, DependencyTree> i) -> i.feature(Gold))
                .createFolds(instances, 1, 0.8);
        VerbNetClassifierTrainer classifier = new VerbNetClassifierTrainer()
                .features(new BaseFeaturePipeline<>(
                        new MultiFeatureFunction<FocusInstance<DepNode, DependencyTree>>()
                                .add(new BiasFeatureFunction<>())
                                .add(new DefaultFeatureFunction<>(
                                        new OffsetContextFactory(Collections.singletonList(0)),
                                        new StringFunctionExtractor<>(
                                                new LookupFeatureExtractor<>(Collections.singletonList(FeatureType.Text.name())),
                                                Collections.singletonList(new LowercaseFunction()))))))
                .classifier(new LibLinearClassifier());
        classifier.train(folds.get(0).getTrainInstances(), folds.get(0).getTestInstances());
    }

}
