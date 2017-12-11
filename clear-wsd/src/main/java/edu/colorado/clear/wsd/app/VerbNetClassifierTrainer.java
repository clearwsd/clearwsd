package edu.colorado.clear.wsd.app;

import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorado.clear.wsd.classifier.PaClassifier;
import edu.colorado.clear.wsd.corpus.VerbNetReader;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
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
import edu.colorado.clear.wsd.feature.resource.DefaultTsvResourceInitializer;
import edu.colorado.clear.wsd.feature.resource.ExtJwnlWordNetResource;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.utils.CountingSenseInventory;
import edu.colorado.clear.wsd.verbnet.PredicateDictionary;
import edu.colorado.clear.wsd.verbnet.VerbNetClassifier;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.feature.extractor.DynamicDependencyNeighborsResource.KEY;

/**
 * VerbNet classifier trainer.
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class VerbNetClassifierTrainer {

    private static final String BWC_PATH = "data/learningResources/bwc.txt";
    private static final String CLUSTER_PATH = "data/learningResources/clusters/";
    private static final List<String> CLUSTERS = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");

    public static FeatureResourceManager resourceManager() {
        FeatureResourceManager resourceManager = new DefaultFeatureResourceManager();
        for (String cluster : CLUSTERS) {
            resourceManager.registerInitializer(cluster, new DefaultTsvResourceInitializer<>(cluster, CLUSTER_PATH + cluster)
                    .keyFunction(new LowercaseFunction()));
        }
        resourceManager.registerInitializer("brown", new BrownClusterResourceInitializer<>("brown", BWC_PATH));
        resourceManager.registerInitializer(ExtJwnlWordNetResource.KEY, ExtJwnlWordNetResource::new);
//        resourceManager.registerInitializer(KEY, new DdnResourceInitializer(new File("data/learningResources/ddnIndex")));
        resourceManager.registerInitializer(KEY,
                new DefaultTsvResourceInitializer<DepNode>(KEY, "data/learningResources/ddnObjects.txt")
                .mappingFunction(new LookupFeatureExtractor<>(FeatureType.Lemma.name()))
        );
        return resourceManager;
    }

    public static FeaturePipeline<FocusInstance<DepNode, DependencyTree>> initializeFeatures() {
        List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> features = new ArrayList<>();
        List<FeatureExtractor<DepNode, List<String>>> filteredDepExtractors = new ArrayList<>();
        for (String cluster : CLUSTERS) {
            filteredDepExtractors.add(new StringListLookupFeature<>(cluster));
        }
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

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features));
    }

    public static void main(String[] args) throws IOException {
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        AggregateAnnotator<FocusInstance<DepNode, DependencyTree>> annotator
                = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        instances.forEach(annotator::annotate);

        CrossValidation<FocusInstance<DepNode, DependencyTree>> cv
                = new CrossValidation<>((FocusInstance<DepNode, DependencyTree> i) -> i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<FocusInstance<DepNode, DependencyTree>>> folds = cv.createFolds(instances, 5);

        NlpClassifier<FocusInstance<DepNode, DependencyTree>> baseClassifier
                = new NlpClassifier<>(new PaClassifier(), initializeFeatures());
        VerbNetClassifier classifier = new VerbNetClassifier(baseClassifier, new CountingSenseInventory(), new PredicateDictionary());
        classifier.train(instances, new ArrayList<>());
        classifier.save(new ObjectOutputStream(new FileOutputStream("data/model.bin")));
        Evaluation overall = new Evaluation(cv.crossValidate(classifier, folds));
        log.info("\n{}", overall.toString());
    }

}
