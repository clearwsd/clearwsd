/*
 * Copyright 2019 James Gung
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.github.clearwsd.feature.pipeline.DefaultFeaturePipeline;
import io.github.clearwsd.feature.pipeline.FeaturePipeline;
import io.github.clearwsd.feature.resource.BrownClusterResourceInitializer;
import io.github.clearwsd.feature.resource.DefaultFeatureResourceManager;
import io.github.clearwsd.feature.resource.DefaultTsvResourceInitializer;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import io.github.clearwsd.feature.resource.WordNetResource;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
 * VerbNet sense classifier feature configurations.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VerbNetFeatureUtils {

    private static final String BASE_PATH = "features/verbnet/";
    private static final String CLUSTER_PATH = BASE_PATH + "clusters/";
    private static final String BWC_PATH = BASE_PATH + "BWC.tsv";
    private static final String DDN_PATH = BASE_PATH + "DDN.tsv";

    private static Set<String> BWC_CLUSTERS = Sets.newHashSet(
            "cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000"
    );

    public static Annotator<NlpFocus<DepNode, DepTree>> defaultAnnotator() {
        List<Annotator<NlpFocus<DepNode, DepTree>>> annotators = new ArrayList<>();
        for (String cluster : BWC_CLUSTERS) {
            annotators.add(new ListAnnotator<>(cluster, lowerForm()));
        }
        annotators.add(new ListAnnotator<>(BWC_KEY, form()));
        annotators.add(new DepNodeListAnnotator<>(DDN_KEY, new DepChildrenContextFactory("dobj", "obj")));
        annotators.add(new DepNodeListAnnotator<>(WN_KEY));
        return new AggregateAnnotator<>(annotators);
    }

    public static FeatureResourceManager defaultResources() {
        FeatureResourceManager resources = new DefaultFeatureResourceManager();
        for (String cluster : BWC_CLUSTERS) {
            resources.registerInitializer(cluster, new DefaultTsvResourceInitializer<>(cluster, getURL(CLUSTER_PATH + cluster))
                    .keyFunction(new LowercaseFunction()));
        }
        resources.registerInitializer(BWC_KEY, new BrownClusterResourceInitializer<>(BWC_KEY, getURL(BWC_PATH)));
        resources.registerInitializer(DDN_KEY, new DefaultTsvResourceInitializer<DepNode>(DDN_KEY, getURL(DDN_PATH))
                .mappingFunction(lemma()));
        resources.registerInitializer(WN_KEY, new WordNetResource.WordNetInitializer<>());
        return resources;
    }

    public static FeaturePipeline<NlpFocus<DepNode, DepTree>> defaultFeatures() {
        // basic extractors
        StringExtractor<DepNode> lemma = lowerLemma();
        StringExtractor<DepNode> dep = lookup(Dep);
        StringExtractor<DepNode> pos = lookup(Pos);

        // semantic extractors applied only to arguments
        List<StringListExtractor<DepNode>> filteredDepExtractors = newArrayList(
                listLookup(BWC_CLUSTERS),
                listLookup(BWC_KEY),
                listLookup(DDN_KEY),
                listLookup(WN_KEY));

        DepContextFactory depContexts = excludingDeps("punct");

        Map<String, List<List<String>>> paths = ImmutableMap.of(
                "dobj", newArrayList(newArrayList("obj"), newArrayList("dobj")));

        DepContextFactory objects = depPath(paths);

        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> features = Arrays.asList(
                cross(function(depContexts, concat(pos, dep))),
                function(window(-2, -1, 1, 2), Arrays.asList(lemma, pos)),
                function(depContexts, concat(dep, Arrays.asList(lemma, pos))),
                function(depContexts, dep),
                function(objects, filteredDepExtractors),
                function(head(), Arrays.asList(dep, lemma, pos)),
                bias());

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features));
    }

    public static FeaturePipeline<NlpFocus<DepNode, DepTree>> sharedFeatures() {
        // basic extractors
        StringExtractor<DepNode> lemma = lowerLemma();
        StringExtractor<DepNode> dep = lookup(Dep);
        StringExtractor<DepNode> pos = lookup(Pos);

        // semantic extractors applied only to arguments
        List<StringListExtractor<DepNode>> filteredDepExtractors = newArrayList(
                listLookup(BWC_CLUSTERS),
                listLookup(BWC_KEY),
                listLookup(DDN_KEY),
                listLookup(WN_KEY));

        DepContextFactory depContexts = excludingDeps("punct");

        Map<String, List<List<String>>> paths = ImmutableMap.of(
                "dobj", newArrayList(newArrayList("obj"), newArrayList("dobj")));

        DepContextFactory objects = depPath(paths);

        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> features = Arrays.asList(
                function(depContexts, concat(dep, Arrays.asList(lemma, pos))),
                function(window(0), Arrays.asList(lemma, pos)),
                function(objects, filteredDepExtractors),
                bias());

        return new DefaultFeaturePipeline<>(new AggregateFeatureFunction<>(features));
    }

    private static URL getURL(String path) {
        return VerbNetFeatureUtils.class.getClassLoader().getResource(path);
    }


}
