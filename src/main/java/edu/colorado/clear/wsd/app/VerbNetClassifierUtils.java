package edu.colorado.clear.wsd.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.LibLinearClassifier;
import edu.colorado.clear.wsd.classifier.MultiClassifier;
import edu.colorado.clear.wsd.corpus.VerbNetReader;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.feature.annotator.ListAnnotator;
import edu.colorado.clear.wsd.feature.context.CompositeContextFactory;
import edu.colorado.clear.wsd.feature.context.DepChildrenContextFactory;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.OffsetContextFactory;
import edu.colorado.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorado.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.NlpFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringFunctionExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListLookupFeature;
import edu.colorado.clear.wsd.feature.extractor.string.LowercaseFunction;
import edu.colorado.clear.wsd.feature.optim.FeaturePipelineFactory;
import edu.colorado.clear.wsd.feature.optim.MetaModelTrainer;
import edu.colorado.clear.wsd.feature.optim.NlpFeaturePipelineFactory;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.verbnet.VerbNetClassifier;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.app.VerbNetClassifierTrainer.resourceManager;

/**
 * Performs a random search for the best configuration with a performance metric and set of parameters.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetClassifierUtils {

    public static List<String> CLUSTERS
            = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");

    public static String BROWN = "brown";

    public static List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> windowUnigrams() {
        return Arrays.asList(
                new OffsetContextFactory(0),
                new OffsetContextFactory(-1, 0),
                new OffsetContextFactory(0, 1),
                new OffsetContextFactory(-1, 1),
                new OffsetContextFactory(-1, 0, 1),
                new OffsetContextFactory(-2, -1, 0, 1),
                new OffsetContextFactory(-1, 0, 1, 2),
                new OffsetContextFactory(-2, -1, 1, 2),
                new OffsetContextFactory(-2, -1, 0, 1, 2)
        );
    }

    public static List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> collocations() {
        return Arrays.asList(
                new CompositeContextFactory<>(
                        new OffsetContextFactory(true, -2, -1),
                        new OffsetContextFactory(true, 1, 2),
                        new OffsetContextFactory(true, -1, 0, 1),
                        new OffsetContextFactory(true, -3, -2, -1),
                        new OffsetContextFactory(true, -2, -1, 0, 1),
                        new OffsetContextFactory(true, -1, 0, 1, 2),
                        new OffsetContextFactory(true, 1, 2, 3)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory(true, -2, -1),
                        new OffsetContextFactory(true, 1, 2),
                        new OffsetContextFactory(true, -1, 0, 1),
                        new OffsetContextFactory(true, -3, -2, -1),
                        new OffsetContextFactory(true, 1, 2, 3)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory(true, -2, -1),
                        new OffsetContextFactory(true, 1, 2),
                        new OffsetContextFactory(true, -1, 1)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory(true, -2, -1),
                        new OffsetContextFactory(true, 1, 2)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory(true, -1, 0),
                        new OffsetContextFactory(true, 0, 1)
                )
        );
    }

    public static List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> filteredContexts(int level) {
        return Stream.of(
                new DepChildrenContextFactory("nsubj", "nsubjpass", "dobj", "iobj", "csubj", "csubjpass", "ccomp", "xcomp",
                        "acl", "nmod", "advcl", "advmod"),
                new DepChildrenContextFactory("nsubj", "nsubjpass", "dobj", "iobj"),
                new DepChildrenContextFactory("nsubj", "nsubjpass", "dobj", "iobj", "acl", "nmod"),
                new DepChildrenContextFactory("nsubj", "nsubjpass", "dobj", "iobj", "acl", "nmod"),
                new DepChildrenContextFactory("nsubj", "nsubjpass", "dobj", "iobj", "acl", "nmod", "advcl", "advmod"),
                new DepChildrenContextFactory("nsubj", "nsubjpass", "dobj", "iobj", "csubj", "csubjpass", "ccomp", "xcomp")
        ).map(f -> f.level(level)).collect(Collectors.toList());
    }

    private static FeaturePipelineFactory<FocusInstance<DepNode, DependencyTree>> getFactory() {
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> windowUnigrams = windowUnigrams();
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> windowBigrams = collocations();
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> depContexts = filteredContexts(0);
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> childModContexts = filteredContexts(1);
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> childSkipModContexts = filteredContexts(2);

        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> rootPath = Collections.singletonList(
                new RootPathContextFactory());

        NlpFeatureExtractor<DepNode, String> text = new LookupFeatureExtractor<>(FeatureType.Text.name());
        NlpFeatureExtractor<DepNode, String> pos = new LookupFeatureExtractor<>(FeatureType.Pos.name());
        NlpFeatureExtractor<DepNode, String> lemma = new LookupFeatureExtractor<>(FeatureType.Lemma.name());
        NlpFeatureExtractor<DepNode, String> dep = new LookupFeatureExtractor<>(FeatureType.Dep.name());

        NlpFeaturePipelineFactory<FocusInstance<DepNode, DependencyTree>, DepNode> featureFunctionFactory
                = new NlpFeaturePipelineFactory<>(0);

        featureFunctionFactory.addBias();

        featureFunctionFactory.addFeatureFunctionFactory(windowUnigrams, text, true)
                .addFeatureFunctionFactory(windowUnigrams, pos, true)
                .addFeatureFunctionFactory(windowUnigrams, lemma, true)
                .addFeatureFunctionFactory(windowUnigrams, dep, true)

                .addFeatureFunctionFactory(windowBigrams, text, true)
                .addFeatureFunctionFactory(windowBigrams, pos, true)
                .addFeatureFunctionFactory(windowBigrams, lemma, true)
                .addFeatureFunctionFactory(windowBigrams, dep, true);

        NlpFeatureExtractor<DepNode, String> textDep = new ConcatenatingFeatureExtractor<>(text, dep);
        NlpFeatureExtractor<DepNode, String> posDep = new ConcatenatingFeatureExtractor<>(pos, dep);
        NlpFeatureExtractor<DepNode, String> lemmaDep = new ConcatenatingFeatureExtractor<>(lemma, dep);

        featureFunctionFactory.addFeatureFunctionFactory(depContexts, textDep, true)
                .addFeatureFunctionFactory(depContexts, posDep, true)
                .addFeatureFunctionFactory(depContexts, lemmaDep, true)

                .addFeatureFunctionFactory(rootPath, pos, true)
                .addFeatureFunctionFactory(rootPath, dep, true)
                .addFeatureFunctionFactory(rootPath, lemma, true);

        NlpFeatureExtractor<DepNode, List<String>> brown = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(BROWN), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster100 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(0)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster320 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(1)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster1000 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(2)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster3200 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(3)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster10000 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(4)), dep);

        featureFunctionFactory.addMultiFeatureFunctionFactory(depContexts, brown, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster100, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster320, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster1000, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster3200, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster10000, true)

                .addFeatureFunctionFactory(childModContexts, posDep, true)
                .addFeatureFunctionFactory(childModContexts, dep, true)
                .addFeatureFunctionFactory(childSkipModContexts, posDep, true)
                .addFeatureFunctionFactory(childSkipModContexts, dep, true);
        return featureFunctionFactory;
    }

    public static List<Annotator<FocusInstance<DepNode, DependencyTree>>> annotators() {
        List<Annotator<FocusInstance<DepNode, DependencyTree>>> annotators = new ArrayList<>();
        for (String cluster : CLUSTERS) {
            annotators.add(new ListAnnotator<>(new StringFunctionExtractor<>(
                    new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction()), cluster));
        }
        annotators.add(new ListAnnotator<>(new LookupFeatureExtractor<>(FeatureType.Text.name()), BROWN));
        return annotators;
    }

    public static void main(String[] args) throws Throwable {
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        AggregateAnnotator<FocusInstance<DepNode, DependencyTree>> annotator
                = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        instances.forEach(annotator::annotate);

        CrossValidation<FocusInstance<DepNode, DependencyTree>> cv
                = new CrossValidation<>((FocusInstance<DepNode, DependencyTree> i) -> (String) i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<FocusInstance<DepNode, DependencyTree>>> folds = cv.createFolds(instances, 5, 0.8);
        FeaturePipelineFactory<FocusInstance<DepNode, DependencyTree>> factory = getFactory();
        MultiClassifier<FocusInstance<DepNode, DependencyTree>, String> multi = new MultiClassifier<>(
                (Serializable & Function<FocusInstance<DepNode, DependencyTree>, String>)
                        (i) -> i.focus().feature(FeatureType.Predicate.name()),
                (Serializable & Supplier<Classifier<FocusInstance<DepNode, DependencyTree>, String>>)
                        () -> new MetaModelTrainer<>(factory, LibLinearClassifier::new, 0));
        VerbNetClassifier classifier = new VerbNetClassifier(multi);
        classifier.train(instances, new ArrayList<>());
        classifier.save(new ObjectOutputStream(new FileOutputStream("data/model.bin")));

        Evaluation overall = new Evaluation(cv.crossValidate(classifier, folds));
        log.info("\n{}", overall.toString());
    }
}
