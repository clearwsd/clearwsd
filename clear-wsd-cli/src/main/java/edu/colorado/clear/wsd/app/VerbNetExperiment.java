package edu.colorado.clear.wsd.app;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorado.clear.type.DepNode;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.type.NlpFocus;
import edu.colorado.clear.wsd.WordSenseClassifier;
import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.PaClassifier;
import edu.colorado.clear.wsd.corpus.semlink.VerbNetReader;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.context.DepChildrenContextFactory;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.OffsetContextFactory;
import edu.colorado.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorado.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListLookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringFunctionExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListExtractor;
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
import edu.colorado.clear.wsd.feature.resource.DynamicDependencyNeighborsResource;
import edu.colorado.clear.wsd.utils.LemmaDictionary;
import edu.colorado.clear.wsd.verbnet.DefaultVerbNetClassifier;
import edu.colorado.clear.wsd.verbnet.VerbNetSenseInventory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.type.FeatureType.Dep;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.resourceManager;

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
        List<NlpFocus<DepNode, DepTree>> trainData = new VerbNetReader().readInstances(
                new FileInputStream("data/datasets/semlink/train.ud.dep"));
        trainData = filterMinCount(MIN_COUNT, trainData); // only consider verbs with > 10 occurrences
        trainData = filterPolysemous(trainData);

        Set<String> verbs = getVerbs(trainData); // only test on verbs in training data
        List<NlpFocus<DepNode, DepTree>> validData = new VerbNetReader().readInstances(
                new FileInputStream("data/datasets/semlink/valid.ud.dep"));
        validData = filterByVerb(verbs, validData);
        List<NlpFocus<DepNode, DepTree>> testData = new VerbNetReader().readInstances(
                new FileInputStream("data/datasets/semlink/test.ud.dep"));
        testData = filterByVerb(verbs, testData);
        log.debug("{} test instances and {} dev instances", testData.size(), validData.size());

        Classifier<NlpFocus<DepNode, DepTree>, String> multi;
        if (single) {
            AggregateAnnotator<NlpFocus<DepNode, DepTree>> annotator
                    = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
            annotator.initialize(resourceManager());
            trainData.forEach(annotator::annotate);
            validData.forEach(annotator::annotate);
            testData.forEach(annotator::annotate);
            multi = new NlpClassifier<>(new PaClassifier(), initializeFeatures());
        } else {
            multi = new DefaultVerbNetClassifier();
        }

        WordSenseClassifier classifier = new WordSenseClassifier(multi, new VerbNetSenseInventory(), new LemmaDictionary());

        classifier.train(trainData, validData);
        classifier.save(new ObjectOutputStream(new FileOutputStream("data/models/semlink.model")));

        // evaluate classifier
        Evaluation evaluation = new Evaluation();
        for (NlpFocus<DepNode, DepTree> instance : validData) {
            evaluation.add(classifier.classify(instance), instance.feature(FeatureType.Gold));
        }
        log.debug("Validation data\n{}", evaluation);

        // ensure that loading classifier gives same results
        classifier = new WordSenseClassifier(new ObjectInputStream(new FileInputStream("data/models/semlink.model")));

        evaluation = new Evaluation();
        for (NlpFocus<DepNode, DepTree> instance : testData) {
            evaluation.add(classifier.classify(instance), instance.feature(FeatureType.Gold));
        }
        log.debug("Test data\n{}", evaluation);
    }

    private static List<NlpFocus<DepNode, DepTree>> filterMinCount(int minCount,
                                                                   List<NlpFocus<DepNode, DepTree>> data) {
        Map<String, Integer> counts = new HashMap<>();
        for (NlpFocus<DepNode, DepTree> instance : data) {
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

    private static List<NlpFocus<DepNode, DepTree>> filterPolysemous(
            List<NlpFocus<DepNode, DepTree>> data) {
        Multimap<String, String> labelMap = HashMultimap.create();
        for (NlpFocus<DepNode, DepTree> instance : data) {
            String predicate = instance.focus().feature(FeatureType.Predicate);
            labelMap.put(predicate, instance.feature(FeatureType.Gold));
        }
        return data.stream()
                .filter(instance -> labelMap.get(instance.focus().feature(FeatureType.Predicate)).size() > 1)
                .collect(Collectors.toList());
    }

    private static List<NlpFocus<DepNode, DepTree>> filterByVerb(Set<String> verbs,
                                                                 List<NlpFocus<DepNode, DepTree>> data) {
        return data.stream()
                .filter(instance -> {
                    String predicate = instance.focus().feature(FeatureType.Predicate);
                    return verbs.contains(predicate);
                })
                .collect(Collectors.toList());
    }

    private static Set<String> getVerbs(List<NlpFocus<DepNode, DepTree>> data) {
        return data.stream()
                .map(i -> (String) i.focus().feature(FeatureType.Predicate))
                .collect(Collectors.toSet());
    }

    private static FeaturePipeline<NlpFocus<DepNode, DepTree>> initializeFeatures() {
        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> features = new ArrayList<>();

        StringExtractor<DepNode> text = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction());
        StringExtractor<DepNode> lemma = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Lemma.name()), new LowercaseFunction());
        StringExtractor<DepNode> dep = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(Dep.name()), new LowercaseFunction());
        StringExtractor<DepNode> pos = new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Pos.name()), new LowercaseFunction());

        List<StringExtractor<DepNode>> windowExtractors =
                Arrays.asList(text, lemma, pos);
        List<StringExtractor<DepNode>> depExtractors =
                Stream.of(lemma, pos).map(s -> new ConcatenatingFeatureExtractor<>(
                        Arrays.asList(s, dep))).collect(Collectors.toList());
        List<StringExtractor<DepNode>> depPathExtractors =
                Arrays.asList(lemma, dep, pos);

        DepChildrenContextFactory depContexts = new DepChildrenContextFactory(
                Sets.newHashSet("punct"), new HashSet<>());
        NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> windowContexts =
                new OffsetContextFactory(Arrays.asList(-2, -1, 0, 1, 2));
        NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> rootPathContext = new RootPathContextFactory(false, 1);

        DepChildrenContextFactory filteredDepContexts = new DepChildrenContextFactory(
                new HashSet<>(), Sets.newHashSet("dobj", "nmod", "xcomp", "advmod"));
        List<StringListExtractor<DepNode>> clusterExtractors =
                Stream.of("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000", "brown").map(
                        (Function<String, ListLookupFeatureExtractor<DepNode>>) ListLookupFeatureExtractor::new)
                        .collect(Collectors.toList());
        clusterExtractors = clusterExtractors.stream()
                .map(s -> new ListConcatenatingFeatureExtractor<>(s, dep))
                .collect(Collectors.toList());
        List<StringListExtractor<DepNode>> filteredDepExtractors = new ArrayList<>(clusterExtractors);
        filteredDepExtractors.add(new ListLookupFeatureExtractor<>(DynamicDependencyNeighborsResource.DDN_KEY));
        filteredDepExtractors.add(new ListLookupFeatureExtractor<>("WN"));

        StringFeatureFunction<NlpFocus<DepNode, DepTree>, DepNode> depFeatures
                = new StringFeatureFunction<>(depContexts, Collections.singletonList(new ConcatenatingFeatureExtractor<>(pos, dep)));
        ConjunctionFunction<NlpFocus<DepNode, DepTree>> function
                = new ConjunctionFunction<>(depFeatures, depFeatures);

        features.add(function);
        features.add(new StringFeatureFunction<>(windowContexts, windowExtractors));
        features.add(new StringFeatureFunction<>(depContexts, depExtractors));
        features.add(new MultiStringFeatureFunction<>(filteredDepContexts, filteredDepExtractors));
        features.add(new MultiStringFeatureFunction<>(new OffsetContextFactory(0), clusterExtractors));
        features.add(new StringFeatureFunction<>(rootPathContext, depPathExtractors));
        features.add(new BiasFeatureFunction<>());

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features));
    }

    public static void main(String... args) throws IOException {
        daisukeSetup();
    }

}
