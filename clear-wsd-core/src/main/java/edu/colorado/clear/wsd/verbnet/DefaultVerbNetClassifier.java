package edu.colorado.clear.wsd.verbnet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import edu.colorado.clear.type.DepNode;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.type.NlpFocus;
import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.Hyperparameter;
import edu.colorado.clear.wsd.classifier.MultiClassifier;
import edu.colorado.clear.wsd.classifier.PaClassifier;
import edu.colorado.clear.wsd.classifier.SparseClassifier;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.feature.annotator.DepNodeListAnnotator;
import edu.colorado.clear.wsd.feature.annotator.ListAnnotator;
import edu.colorado.clear.wsd.feature.context.DepChildrenContextFactory;
import edu.colorado.clear.wsd.feature.context.DepContextFactory;
import edu.colorado.clear.wsd.feature.extractor.Extractors;
import edu.colorado.clear.wsd.feature.extractor.StringExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListExtractor;
import edu.colorado.clear.wsd.feature.extractor.string.LowercaseFunction;
import edu.colorado.clear.wsd.feature.function.AggregateFeatureFunction;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.pipeline.AnnotatingClassifier;
import edu.colorado.clear.wsd.feature.pipeline.DefaultFeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import edu.colorado.clear.wsd.feature.resource.BrownClusterResourceInitializer;
import edu.colorado.clear.wsd.feature.resource.DefaultFeatureResourceManager;
import edu.colorado.clear.wsd.feature.resource.DefaultTsvResourceInitializer;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.feature.resource.WordNetResource.WordNetInitializer;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.type.FeatureType.Dep;
import static edu.colorado.clear.type.FeatureType.Pos;
import static edu.colorado.clear.wsd.feature.context.Contexts.excludingDeps;
import static edu.colorado.clear.wsd.feature.context.Contexts.focus;
import static edu.colorado.clear.wsd.feature.context.Contexts.head;
import static edu.colorado.clear.wsd.feature.context.Contexts.includingDeps;
import static edu.colorado.clear.wsd.feature.context.Contexts.window;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.concat;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.form;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.lemma;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.listConcat;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.listLookup;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.lookup;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.lowerForm;
import static edu.colorado.clear.wsd.feature.extractor.Extractors.lowerLemma;
import static edu.colorado.clear.wsd.feature.function.Features.bias;
import static edu.colorado.clear.wsd.feature.function.Features.cross;
import static edu.colorado.clear.wsd.feature.function.Features.function;
import static edu.colorado.clear.wsd.feature.resource.BrownClusterResourceInitializer.BWC_KEY;
import static edu.colorado.clear.wsd.feature.resource.DynamicDependencyNeighborsResource.DDN_KEY;
import static edu.colorado.clear.wsd.feature.resource.WordNetResource.WN_KEY;

/**
 * Default VerbNet classifier.
 *
 * @author jamesgung
 */
@Slf4j
public class DefaultVerbNetClassifier implements Classifier<NlpFocus<DepNode, DepTree>, String> {

    private static final long serialVersionUID = -3815702452161005214L;

    private static final String BASE_PATH = "features/verbnet/";
    private static final String CLUSTER_PATH = BASE_PATH + "clusters/";
    private static final String BWC_PATH = BASE_PATH + "BWC.tsv";
    private static final String DDN_PATH = BASE_PATH + "DDN.tsv";

    private Set<String> clusters = Sets.newHashSet("cluster-100", "cluster-320", "cluster-1000", "cluster-3200",
            "cluster-10000");
    private Set<String> includedRels = Sets.newHashSet("dobj");
    private Set<String> excludedRels = Sets.newHashSet("punct");
    private Set<Integer> offsets = Sets.newHashSet(-2, -1, 0, 1, 2);

    private AnnotatingClassifier<NlpFocus<DepNode, DepTree>> classifier;
    private FeatureResourceManager resources;

    public DefaultVerbNetClassifier() {
        resources = initializeResources();
        classifier = initialize();
        classifier.initialize(resources);
    }

    private AnnotatingClassifier<NlpFocus<DepNode, DepTree>> initialize() {
        MultiClassifier<NlpFocus<DepNode, DepTree>, String> multiClassifier
                = new MultiClassifier<>((Serializable & Function<NlpFocus<DepNode, DepTree>, String>)
                (i) -> i.focus().feature(FeatureType.Predicate),
                (Serializable & Supplier<Classifier<NlpFocus<DepNode, DepTree>, String>>)
                        () -> new NlpClassifier<>(initializeClassifier(), initializeFeatures()));
        return new AnnotatingClassifier<>(multiClassifier, initializeAnnotator());
    }

    @Override
    public String classify(NlpFocus<DepNode, DepTree> instance) {
        return classifier.classify(instance);
    }

    @Override
    public Map<String, Double> score(NlpFocus<DepNode, DepTree> instance) {
        return classifier.score(instance);
    }

    @Override
    public void train(List<NlpFocus<DepNode, DepTree>> train, List<NlpFocus<DepNode, DepTree>> valid) {
        classifier.train(train, valid);
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
            classifier = (AnnotatingClassifier<NlpFocus<DepNode, DepTree>>) inputStream.readObject();
            resources = (FeatureResourceManager) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
            outputStream.writeObject(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FeatureResourceManager initializeResources() {
        FeatureResourceManager resources = new DefaultFeatureResourceManager();
        for (String cluster : clusters) {
            resources.registerInitializer(cluster, new DefaultTsvResourceInitializer<>(cluster, getURL(CLUSTER_PATH + cluster))
                    .keyFunction(new LowercaseFunction()));
        }
        resources.registerInitializer(BWC_KEY, new BrownClusterResourceInitializer<>(BWC_KEY, getURL(BWC_PATH)));
        resources.registerInitializer(DDN_KEY, new DefaultTsvResourceInitializer<DepNode>(DDN_KEY, getURL(DDN_PATH))
                .mappingFunction(lemma()));
        resources.registerInitializer(WN_KEY, new WordNetInitializer<>());
        return resources;
    }

    private Annotator<NlpFocus<DepNode, DepTree>> initializeAnnotator() {
        List<Annotator<NlpFocus<DepNode, DepTree>>> annotators = new ArrayList<>();
        for (String cluster : clusters) {
            annotators.add(new ListAnnotator<>(cluster, lowerForm()));
        }
        annotators.add(new ListAnnotator<>(BWC_KEY, form()));
        annotators.add(new DepNodeListAnnotator<>(DDN_KEY, new DepChildrenContextFactory(includedRels)));
        annotators.add(new DepNodeListAnnotator<>(WN_KEY));
        return new AggregateAnnotator<>(annotators);
    }

    private FeaturePipeline<NlpFocus<DepNode, DepTree>> initializeFeatures() {
        // basic extractors
        StringExtractor<DepNode> text = lowerForm();
        StringExtractor<DepNode> lemma = lowerLemma();
        StringExtractor<DepNode> dep = lookup(Dep);
        StringExtractor<DepNode> pos = lookup(Pos);

        // semantic extractors applied to focus and arguments
        Set<String> keys = new HashSet<>(clusters);
        keys.add(BWC_KEY);
        StringListExtractor<DepNode> clusterExtractors = listConcat(Extractors.listLookup(keys), dep);
        // semantic extractors applied only to arguments
        List<StringListExtractor<DepNode>> filteredDepExtractors = Lists.newArrayList(clusterExtractors);
        filteredDepExtractors.add(listLookup(DDN_KEY));
        filteredDepExtractors.add(listLookup(WN_KEY));

        DepContextFactory depContexts = excludingDeps(excludedRels);

        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> features = Arrays.asList(
                cross(function(depContexts, concat(pos, dep))),
                function(window(offsets), Arrays.asList(text, lemma, pos)),
                function(depContexts, concat(dep, Arrays.asList(lemma, pos))),
                function(depContexts, dep),
                function(includingDeps(includedRels), filteredDepExtractors),
                function(focus(), clusterExtractors),
                function(head(), Arrays.asList(dep, lemma, pos)),
                bias());

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features));
    }

    private SparseClassifier initializeClassifier() {
        return new PaClassifier();
    }

    private URL getURL(String path) {
        return this.getClass().getClassLoader().getResource(path);
    }

}