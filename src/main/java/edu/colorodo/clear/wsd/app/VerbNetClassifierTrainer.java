package edu.colorodo.clear.wsd.app;

import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorodo.clear.wsd.classifier.LibLinearClassifier;
import edu.colorodo.clear.wsd.classifier.SparseClassifier;
import edu.colorodo.clear.wsd.classifier.StringInstance;
import edu.colorodo.clear.wsd.corpus.VerbNetReader;
import edu.colorodo.clear.wsd.eval.CrossValidation;
import edu.colorodo.clear.wsd.eval.Evaluation;
import edu.colorodo.clear.wsd.feature.annotator.Annotator;
import edu.colorodo.clear.wsd.feature.annotator.ListAnnotator;
import edu.colorodo.clear.wsd.feature.context.DepChildrenContextFactory;
import edu.colorodo.clear.wsd.feature.context.NlpContextFactory;
import edu.colorodo.clear.wsd.feature.context.OffsetContextFactory;
import edu.colorodo.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorodo.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorodo.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorodo.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorodo.clear.wsd.feature.extractor.StringFunctionExtractor;
import edu.colorodo.clear.wsd.feature.extractor.StringListLookupFeature;
import edu.colorodo.clear.wsd.feature.extractor.string.LowercaseFunction;
import edu.colorodo.clear.wsd.feature.function.AggregateFeatureFunction;
import edu.colorodo.clear.wsd.feature.function.BiasFeatureFunction;
import edu.colorodo.clear.wsd.feature.function.FeatureFunction;
import edu.colorodo.clear.wsd.feature.function.MultiStringFeatureFunction;
import edu.colorodo.clear.wsd.feature.function.StringFeatureFunction;
import edu.colorodo.clear.wsd.feature.pipeline.DefaultFeaturePipeline;
import edu.colorodo.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorodo.clear.wsd.feature.resource.MultimapResource;
import edu.colorodo.clear.wsd.feature.util.BrownClusterResourceInitializer;
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

    private static FeaturePipeline<FocusInstance<DepNode, DependencyTree>> initializeFeatures() throws FileNotFoundException {
        List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> features = new ArrayList<>();

        List<FeatureExtractor<DepNode, List<String>>> filteredDepExtractors = new ArrayList<>();
        List<String> clusters = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");
        List<Annotator<FocusInstance<DepNode, DependencyTree>>> annotators = new ArrayList<>();
        for (String cluster : clusters) {
            MultimapResource<String> multimapResource = new MultimapResource<>(cluster);
            multimapResource.keyFunction(String::toLowerCase);
            multimapResource.initialize(new FileInputStream("data/learningResources/clusters/" + cluster));
            annotators.add(new ListAnnotator<>(new StringFunctionExtractor<>(
                    new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction()), multimapResource));
            filteredDepExtractors.add(new StringListLookupFeature<>(cluster));
        }
        MultimapResource<String> multimapResource = new MultimapResource<>("brown");
        multimapResource.initializer(new BrownClusterResourceInitializer<>());
        multimapResource.initialize(new FileInputStream("data/learningResources/bwc.txt"));
        annotators.add(new ListAnnotator<>(new LookupFeatureExtractor<>(FeatureType.Text.name()), multimapResource));
        filteredDepExtractors.add(new StringListLookupFeature<>("brown"));

        FeatureExtractor<DepNode, String> text = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction());
        FeatureExtractor<DepNode, String> lemma = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Lemma.name()), new LowercaseFunction());
        FeatureExtractor<DepNode, String> dep = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Dep.name()), new LowercaseFunction());
        FeatureExtractor<DepNode, String> pos = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Pos.name()), new LowercaseFunction());

        List<FeatureExtractor<DepNode, String>> windowExtractors =
                Arrays.asList(text, lemma, dep, pos);
        List<FeatureExtractor<DepNode, String>> depExtractors =
                Stream.of(text, lemma, pos).map(s -> new ConcatenatingFeatureExtractor<>(
                        Arrays.asList(s, dep))).collect(Collectors.toList());
        List<FeatureExtractor<DepNode, String>> depPathExtractors =
                Arrays.asList(dep, pos);

        NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> filteredDepContexts = new DepChildrenContextFactory(
                new HashSet<>(), Sets.newHashSet("nsubj", "dobj", "nsubjpass", "iobj", "nmod"));
        NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> depContexts = new DepChildrenContextFactory();
        NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> windowContexts =
                new OffsetContextFactory(Arrays.asList(-2, -1, 0, 1, 2));
        NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> rootPathContext = new RootPathContextFactory();

        features.add(new StringFeatureFunction<>(windowContexts, windowExtractors));
        features.add(new StringFeatureFunction<>(depContexts, depExtractors));
        features.add(new MultiStringFeatureFunction<>(filteredDepContexts, filteredDepExtractors));
        features.add(new StringFeatureFunction<>(rootPathContext, depPathExtractors));
        features.add(new BiasFeatureFunction<>());

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features), annotators);
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        List<CrossValidation.Fold<FocusInstance<DepNode, DependencyTree>>> folds
                = new CrossValidation<>((FocusInstance<DepNode, DependencyTree> i) -> (String) i.feature(Gold))
                .createFolds(instances, 1, 0.8);
        VerbNetClassifierTrainer classifier = new VerbNetClassifierTrainer()
                .classifier(new LibLinearClassifier())
                .features(initializeFeatures());
        classifier.train(folds.get(0).getTrainInstances(), folds.get(0).getTestInstances());
    }

}
