package edu.colorado.clear.wsd.app;

import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorado.clear.wsd.classifier.LibLinearClassifier;
import edu.colorado.clear.wsd.corpus.VerbNetReader;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.feature.annotator.ListAnnotator;
import edu.colorado.clear.wsd.feature.context.DepChildrenContextFactory;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.OffsetContextFactory;
import edu.colorado.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorado.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringFunctionExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListLookupFeature;
import edu.colorado.clear.wsd.feature.extractor.string.LowercaseFunction;
import edu.colorado.clear.wsd.feature.function.AggregateFeatureFunction;
import edu.colorado.clear.wsd.feature.function.BiasFeatureFunction;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.function.MultiStringFeatureFunction;
import edu.colorado.clear.wsd.feature.function.StringFeatureFunction;
import edu.colorado.clear.wsd.feature.pipeline.DefaultFeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import edu.colorado.clear.wsd.feature.resource.BrownClusterResourceInitializer;
import edu.colorado.clear.wsd.feature.resource.DefaultFeatureResourceManager;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.feature.resource.MultimapResource;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet classifier trainer.
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class VerbNetClassifierTrainer {

    public static FeatureResourceManager resourceManager() {
        try {
            DefaultFeatureResourceManager resourceManager = new DefaultFeatureResourceManager();
            List<String> clusters = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");
            for (String cluster : clusters) {
                MultimapResource<String> multimapResource = new MultimapResource<>(cluster);
                multimapResource.keyFunction(new LowercaseFunction());
                multimapResource.initialize(new FileInputStream("data/learningResources/clusters/" + cluster));
                resourceManager.addResource(cluster, multimapResource);
            }
            MultimapResource<String> multimapResource = new MultimapResource<>("brown");
            multimapResource.initializer(new BrownClusterResourceInitializer<>());
            multimapResource.initialize(new FileInputStream("data/learningResources/bwc.txt"));
            resourceManager.addResource("brown", multimapResource);
            return resourceManager;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static FeaturePipeline<FocusInstance<DepNode, DependencyTree>> initializeFeatures() throws FileNotFoundException {
        List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> features = new ArrayList<>();
        List<FeatureExtractor<DepNode, List<String>>> filteredDepExtractors = new ArrayList<>();
        List<String> clusters = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");
        List<Annotator<FocusInstance<DepNode, DependencyTree>>> annotators = new ArrayList<>();
        for (String cluster : clusters) {
            annotators.add(new ListAnnotator<>(new StringFunctionExtractor<>(
                    new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction()), cluster));
            filteredDepExtractors.add(new StringListLookupFeature<>(cluster));
        }
        annotators.add(new ListAnnotator<>(new LookupFeatureExtractor<>(FeatureType.Text.name()), "brown"));
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

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features),
                new AggregateAnnotator<>(annotators), resourceManager());
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        CrossValidation<FocusInstance<DepNode, DependencyTree>> cv
                = new CrossValidation<>((FocusInstance<DepNode, DependencyTree> i) -> (String) i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<FocusInstance<DepNode, DependencyTree>>> folds = cv.createFolds(instances, 5, 0.8);

        NlpClassifier<FocusInstance<DepNode, DependencyTree>> classifier
                = new NlpClassifier<>(new LibLinearClassifier(), initializeFeatures());
        Evaluation overall = new Evaluation(cv.crossValidate(classifier, folds));
        log.info(overall.toString());
    }

}
