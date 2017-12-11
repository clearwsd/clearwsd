package edu.colorado.clear.wsd.app.experiment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorado.clear.wsd.app.VerbNetClassifierUtils;
import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.MultiClassifier;
import edu.colorado.clear.wsd.classifier.PaClassifier;
import edu.colorado.clear.wsd.corpus.VerbNetReader;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.context.DepChildrenContextFactory;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.OffsetContextFactory;
import edu.colorado.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorado.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringFunctionExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListLookupFeature;
import edu.colorado.clear.wsd.feature.extractor.string.LowercaseFunction;
import edu.colorado.clear.wsd.feature.function.AggregateFeatureFunction;
import edu.colorado.clear.wsd.feature.function.BiasFeatureFunction;
import edu.colorado.clear.wsd.feature.function.ConjunctionFunction;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.function.MultiStringFeatureFunction;
import edu.colorado.clear.wsd.feature.function.StringFeatureFunction;
import edu.colorado.clear.wsd.feature.pipeline.DefaultFeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.verbnet.PredicateDictionary;
import edu.colorado.clear.wsd.verbnet.VerbNetClassifier;
import edu.colorado.clear.wsd.verbnet.VerbNetSenseInventory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.app.VerbNetClassifierTrainer.resourceManager;
import static edu.colorado.clear.wsd.type.FeatureType.Dep;

/**
 * VerbNet experiment builder.
 *
 * @author jamesgung
 */
@Slf4j
@Getter
@Setter
@Accessors(fluent = true)
public class VerbNetExperiment {

    private static final int MIN_COUNT = 10;
    private static boolean single = false;

    private int minPredicateCount;
    private boolean filterTestVerbs;

    private static void daisukeSetup() throws IOException {
        List<FocusInstance<DepNode, DependencyTree>> trainData = new VerbNetReader().readInstances(
                new FileInputStream("data/datasets/semlink/train.ud.dep"));
        trainData = filterMinCount(MIN_COUNT, trainData); // only consider verbs with > 10 occurrences
        trainData = filterPolysemous(trainData);

        Set<String> verbs = getVerbs(trainData); // only test on verbs in training data
        List<FocusInstance<DepNode, DependencyTree>> validData = new VerbNetReader().readInstances(
                new FileInputStream("data/datasets/semlink/valid.ud.dep"));
        validData = filterByVerb(verbs, validData);
        List<FocusInstance<DepNode, DependencyTree>> testData = new VerbNetReader().readInstances(
                new FileInputStream("data/datasets/semlink/test.ud.dep"));
        testData = filterByVerb(verbs, testData);

        AggregateAnnotator<FocusInstance<DepNode, DependencyTree>> annotator
                = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        trainData.forEach(annotator::annotate);
        validData.forEach(annotator::annotate);
        testData.forEach(annotator::annotate);

        Classifier<FocusInstance<DepNode, DependencyTree>, String> multi;
        if (single) {
            multi = new NlpClassifier<>(new PaClassifier(), initializeFeatures());
        } else {
            multi = new MultiClassifier<>(
                    (Serializable & Function<FocusInstance<DepNode, DependencyTree>, String>)
                            (i) -> i.focus().feature(FeatureType.Predicate),
                    (Serializable & Supplier<Classifier<FocusInstance<DepNode, DependencyTree>, String>>)
                            () -> new NlpClassifier<>(new PaClassifier(), initializeFeatures()));
//            FeaturePipelineFactory<FocusInstance<DepNode, DependencyTree>> factory = getFactory();
//            multi = new MultiClassifier<>(
//                    (Serializable & Function<FocusInstance<DepNode, DependencyTree>, String>)
//                            (i) -> i.focus().feature(FeatureType.Predicate),
//                    (Serializable & Supplier<Classifier<FocusInstance<DepNode, DependencyTree>, String>>)
//                            () -> new MetaModelTrainer<>(factory, PaClassifier::new));
        }

        VerbNetClassifier classifier = new VerbNetClassifier(multi, new VerbNetSenseInventory(), new PredicateDictionary());

        classifier.train(trainData, validData);
        classifier.save(new ObjectOutputStream(new FileOutputStream("data/models/semlink.model")));

        // evaluate classifier
        Evaluation evaluation = new Evaluation();
        for (FocusInstance<DepNode, DependencyTree> instance : validData) {
            evaluation.add(classifier.classify(instance), instance.feature(FeatureType.Gold));
        }
        log.debug("Validation data\n{}", evaluation);

        // ensure that loading classifier gives same results
        classifier = new VerbNetClassifier(new ObjectInputStream(new FileInputStream("data/models/semlink.model")));

        evaluation = new Evaluation();
        for (FocusInstance<DepNode, DependencyTree> instance : testData) {
            evaluation.add(classifier.classify(instance), instance.feature(FeatureType.Gold));
        }
        log.debug("Test data\n{}", evaluation);
    }

    private static List<FocusInstance<DepNode, DependencyTree>> filterMinCount(int minCount,
                                                                               List<FocusInstance<DepNode, DependencyTree>> data) {
        Map<String, Integer> counts = new HashMap<>();
        for (FocusInstance<DepNode, DependencyTree> instance : data) {
            String lemma = instance.focus().feature(FeatureType.Predicate);
            counts.merge(lemma, 1, (prev, one) -> prev + one);
        }
        return data.stream()
                .filter(instance -> {
                    String predicate = instance.focus().feature(FeatureType.Predicate);
                    return counts.get(predicate) >= minCount;
                })
                .collect(Collectors.toList());
    }

    private static List<FocusInstance<DepNode, DependencyTree>> filterPolysemous(
            List<FocusInstance<DepNode, DependencyTree>> data) {
        Multimap<String, String> labelMap = HashMultimap.create();
        for (FocusInstance<DepNode, DependencyTree> instance : data) {
            String predicate = instance.focus().feature(FeatureType.Predicate);
            labelMap.put(predicate, instance.feature(FeatureType.Gold));
        }
        return data.stream()
                .filter(instance -> labelMap.get(instance.focus().feature(FeatureType.Predicate)).size() > 1)
                .collect(Collectors.toList());
    }

    private static List<FocusInstance<DepNode, DependencyTree>> filterByVerb(Set<String> verbs,
                                                                             List<FocusInstance<DepNode, DependencyTree>> data) {
        return data.stream()
                .filter(instance -> {
                    String predicate = instance.focus().feature(FeatureType.Predicate);
                    return verbs.contains(predicate);
                })
                .collect(Collectors.toList());
    }

    private static Set<String> getVerbs(List<FocusInstance<DepNode, DependencyTree>> data) {
        return data.stream()
                .map(i -> (String) i.focus().feature(FeatureType.Predicate))
                .collect(Collectors.toSet());
    }

    public static FeaturePipeline<FocusInstance<DepNode, DependencyTree>> initializeFeatures() {
        List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> features = new ArrayList<>();

        FeatureExtractor<DepNode, String> text = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction());
        FeatureExtractor<DepNode, String> lemma = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Lemma.name()), new LowercaseFunction());
        FeatureExtractor<DepNode, String> dep = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(Dep.name()), new LowercaseFunction());
        FeatureExtractor<DepNode, String> pos = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Pos.name()), new LowercaseFunction());

        List<FeatureExtractor<DepNode, String>> windowExtractors =
                Arrays.asList(text, lemma, pos);
        List<FeatureExtractor<DepNode, String>> depExtractors =
                Stream.of(lemma, pos).map(s -> new ConcatenatingFeatureExtractor<>(
                        Arrays.asList(s, dep))).collect(Collectors.toList());
        List<FeatureExtractor<DepNode, String>> depPathExtractors =
                Arrays.asList(lemma, dep, pos);

        DepChildrenContextFactory filteredDepContexts = new DepChildrenContextFactory(
                new HashSet<>(), Sets.newHashSet("dobj", "iobj", "nmod", "xcomp", "advmod"));
        DepChildrenContextFactory depContexts = new DepChildrenContextFactory(
                Sets.newHashSet("punct"), new HashSet<>());
        NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> windowContexts =
                new OffsetContextFactory(Arrays.asList(-2, -1, 0, 1, 2));
        NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> rootPathContext = new RootPathContextFactory(false, 1);
        List<FeatureExtractor<DepNode, List<String>>> filteredDepExtractors =
                Stream.of("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000", "brown").map(
                        (Function<String, StringListLookupFeature<DepNode>>) StringListLookupFeature::new)
                        .collect(Collectors.toList());
        filteredDepExtractors = filteredDepExtractors.stream()
                .map(s -> new ListConcatenatingFeatureExtractor<>(s, dep))
                .collect(Collectors.toList());
//        filteredDepExtractors.add(new StringListLookupFeature<>(DynamicDependencyNeighborsResource.KEY));
        filteredDepExtractors.add(new StringListLookupFeature<>("WN"));

        StringFeatureFunction<FocusInstance<DepNode, DependencyTree>, DepNode> depFeatures
                = new StringFeatureFunction<>(depContexts, Collections.singletonList(new ConcatenatingFeatureExtractor<>(pos, dep)));
        ConjunctionFunction<FocusInstance<DepNode, DependencyTree>> function
                = new ConjunctionFunction<>(depFeatures, depFeatures);

        features.add(function);
        features.add(new StringFeatureFunction<>(windowContexts, windowExtractors));
        features.add(new StringFeatureFunction<>(depContexts, depExtractors));
        features.add(new MultiStringFeatureFunction<>(filteredDepContexts, filteredDepExtractors));
        features.add(new MultiStringFeatureFunction<>(new OffsetContextFactory(0), filteredDepExtractors));
        features.add(new StringFeatureFunction<>(rootPathContext, depPathExtractors));
        features.add(new BiasFeatureFunction<>());

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features));
    }

    public static void main(String... args) throws IOException {
        daisukeSetup();
    }

}
