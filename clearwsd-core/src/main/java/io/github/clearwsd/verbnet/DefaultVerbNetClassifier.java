/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.verbnet;

import com.google.common.collect.ImmutableMap;
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

import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.Hyperparameter;
import io.github.clearwsd.classifier.MultiClassifier;
import io.github.clearwsd.classifier.PaClassifier;
import io.github.clearwsd.classifier.SparseClassifier;
import io.github.clearwsd.feature.annotator.AggregateAnnotator;
import io.github.clearwsd.feature.annotator.Annotator;
import io.github.clearwsd.feature.annotator.DepNodeListAnnotator;
import io.github.clearwsd.feature.annotator.ListAnnotator;
import io.github.clearwsd.feature.context.DepChildrenContextFactory;
import io.github.clearwsd.feature.context.DepContextFactory;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.feature.extractor.string.LowercaseFunction;
import io.github.clearwsd.feature.function.AggregateFeatureFunction;
import io.github.clearwsd.feature.function.FeatureFunction;
import io.github.clearwsd.feature.pipeline.AnnotatingClassifier;
import io.github.clearwsd.feature.pipeline.DefaultFeaturePipeline;
import io.github.clearwsd.feature.pipeline.FeaturePipeline;
import io.github.clearwsd.feature.pipeline.NlpClassifier;
import io.github.clearwsd.feature.resource.BrownClusterResourceInitializer;
import io.github.clearwsd.feature.resource.DefaultFeatureResourceManager;
import io.github.clearwsd.feature.resource.DefaultTsvResourceInitializer;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import io.github.clearwsd.feature.resource.WordNetResource.WordNetInitializer;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.clearwsd.feature.context.Contexts.depPath;
import static io.github.clearwsd.feature.context.Contexts.excludingDeps;
import static io.github.clearwsd.feature.context.Contexts.head;
import static io.github.clearwsd.feature.context.Contexts.window;
import static io.github.clearwsd.feature.extractor.Extractors.concat;
import static io.github.clearwsd.feature.extractor.Extractors.form;
import static io.github.clearwsd.feature.extractor.Extractors.lemma;
import static io.github.clearwsd.feature.extractor.Extractors.listLookup;
import static io.github.clearwsd.feature.extractor.Extractors.lookup;
import static io.github.clearwsd.feature.extractor.Extractors.lowerForm;
import static io.github.clearwsd.feature.extractor.Extractors.lowerLemma;
import static io.github.clearwsd.feature.function.Features.bias;
import static io.github.clearwsd.feature.function.Features.cross;
import static io.github.clearwsd.feature.function.Features.function;
import static io.github.clearwsd.feature.resource.BrownClusterResourceInitializer.BWC_KEY;
import static io.github.clearwsd.feature.resource.DynamicDependencyNeighborsResource.DDN_KEY;
import static io.github.clearwsd.feature.resource.WordNetResource.WN_KEY;
import static io.github.clearwsd.type.FeatureType.Dep;
import static io.github.clearwsd.type.FeatureType.Pos;

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
    private Set<String> includedRels = Sets.newHashSet("dobj", "obj");
    private Set<String> excludedRels = Sets.newHashSet("punct");
    private Set<Integer> offsets = Sets.newHashSet(-2, -1, 1, 2);

    private AnnotatingClassifier<NlpFocus<DepNode, DepTree>> classifier;
    private FeatureResourceManager resources;

    public DefaultVerbNetClassifier() {
        resources = initializeResources();
        resources.initialize();
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
            resources.initialize();
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
        StringExtractor<DepNode> lemma = lowerLemma();
        StringExtractor<DepNode> dep = lookup(Dep);
        StringExtractor<DepNode> pos = lookup(Pos);

        // semantic extractors applied only to arguments
        List<StringListExtractor<DepNode>> filteredDepExtractors = newArrayList(
                listLookup(new HashSet<>(clusters)),
                listLookup(BWC_KEY),
                listLookup(DDN_KEY),
                listLookup(WN_KEY));

        DepContextFactory depContexts = excludingDeps(excludedRels);

        Map<String, List<List<String>>> paths = ImmutableMap.of(
            "dobj", newArrayList(newArrayList("obj"), newArrayList("dobj")));

        DepContextFactory objects = depPath(paths);

        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> features = Arrays.asList(
                cross(function(depContexts, concat(pos, dep))),
                function(window(offsets), Arrays.asList(lemma, pos)),
                function(depContexts, concat(dep, Arrays.asList(lemma, pos))),
                function(depContexts, dep),
                function(objects, filteredDepExtractors),
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
