/*
 * Copyright (C) 2017  James Gung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.clearwsd.app;

import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.clearwsd.WordSenseClassifier;
import io.github.clearwsd.classifier.Classifier;
import io.github.clearwsd.classifier.LibLinearClassifier;
import io.github.clearwsd.classifier.MultiClassifier;
import io.github.clearwsd.corpus.semlink.VerbNetReader;
import io.github.clearwsd.eval.CrossValidation;
import io.github.clearwsd.eval.Evaluation;
import io.github.clearwsd.feature.annotator.AggregateAnnotator;
import io.github.clearwsd.feature.annotator.Annotator;
import io.github.clearwsd.feature.annotator.DepNodeListAnnotator;
import io.github.clearwsd.feature.annotator.ListAnnotator;
import io.github.clearwsd.feature.context.CompositeContextFactory;
import io.github.clearwsd.feature.context.DepChildrenContextFactory;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.context.OffsetContextFactory;
import io.github.clearwsd.feature.context.RootPathContextFactory;
import io.github.clearwsd.feature.extractor.ConcatenatingFeatureExtractor;
import io.github.clearwsd.feature.extractor.ListConcatenatingFeatureExtractor;
import io.github.clearwsd.feature.extractor.ListLookupFeatureExtractor;
import io.github.clearwsd.feature.extractor.LookupFeatureExtractor;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringFunctionExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.feature.extractor.string.LowercaseFunction;
import io.github.clearwsd.feature.optim.FeaturePipelineFactory;
import io.github.clearwsd.feature.optim.MetaModelTrainer;
import io.github.clearwsd.feature.optim.NlpFeaturePipelineFactory;
import io.github.clearwsd.feature.resource.BrownClusterResourceInitializer;
import io.github.clearwsd.feature.resource.DefaultFeatureResourceManager;
import io.github.clearwsd.feature.resource.DefaultTsvResourceInitializer;
import io.github.clearwsd.feature.resource.DynamicDependencyNeighborsResource;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import io.github.clearwsd.feature.resource.WordNetResource;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.utils.CountingSenseInventory;
import io.github.clearwsd.utils.LemmaDictionary;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.feature.resource.DynamicDependencyNeighborsResource.DDN_KEY;

/**
 * Performs a random search for the best configuration with a performance metric and set of parameters.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetClassifierUtils {

    static List<String> CLUSTERS
            = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");

    static String BROWN = "brown";

    private static final String BWC_PATH = "data/learningResources/bwc.txt";
    private static final String CLUSTER_PATH = "data/learningResources/clusters/";

    static List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowUnigrams() {
        return Arrays.asList(
                new OffsetContextFactory<>(0),
                new OffsetContextFactory<>(-1, 0),
                new OffsetContextFactory<>(0, 1),
                new OffsetContextFactory<>(-1, 1),
                new OffsetContextFactory<>(-1, 0, 1),
                new OffsetContextFactory<>(-2, -1, 0, 1),
                new OffsetContextFactory<>(-1, 0, 1, 2),
                new OffsetContextFactory<>(-2, -1, 1, 2),
                new OffsetContextFactory<>(-2, -1, 0, 1, 2)
        );
    }

    static List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> collocations() {
        return Arrays.asList(
                new CompositeContextFactory<>(
                        new OffsetContextFactory<>(true, -2, -1),
                        new OffsetContextFactory<>(true, 1, 2),
                        new OffsetContextFactory<>(true, -1, 0, 1),
                        new OffsetContextFactory<>(true, -3, -2, -1),
                        new OffsetContextFactory<>(true, -2, -1, 0, 1),
                        new OffsetContextFactory<>(true, -1, 0, 1, 2),
                        new OffsetContextFactory<>(true, 1, 2, 3)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory<>(true, -2, -1),
                        new OffsetContextFactory<>(true, 1, 2),
                        new OffsetContextFactory<>(true, -1, 0, 1),
                        new OffsetContextFactory<>(true, -3, -2, -1),
                        new OffsetContextFactory<>(true, 1, 2, 3)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory<>(true, -2, -1),
                        new OffsetContextFactory<>(true, 1, 2),
                        new OffsetContextFactory<>(true, -1, 1)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory<>(true, -2, -1),
                        new OffsetContextFactory<>(true, 1, 2)
                ),
                new CompositeContextFactory<>(
                        new OffsetContextFactory<>(true, -1, 0),
                        new OffsetContextFactory<>(true, 0, 1)
                )
        );
    }

    static List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> filteredContexts(int level) {
        return Stream.of(
                new DepChildrenContextFactory("dobj", "iobj", "nmod", "xcomp", "advmod"),
                new DepChildrenContextFactory("dobj", "iobj", "nmod", "xcomp", "advmod", "nsubj", "nsubjpass"),
                new DepChildrenContextFactory("dobj", "iobj", "nmod", "xcomp", "advmod", "nsubj", "nsubjpass", "advcl"),
                new DepChildrenContextFactory("dobj", "nmod", "xcomp", "iobj"),
                new DepChildrenContextFactory("dobj", "nmod"),
                new DepChildrenContextFactory("dobj")
        ).map(f -> f.level(level)).collect(Collectors.toList());
    }

    private static FeaturePipelineFactory<NlpFocus<DepNode, DepTree>> getFactory() {
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowUnigrams = windowUnigrams();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowBigrams = collocations();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> allDeps =
                Collections.singletonList(new DepChildrenContextFactory(Sets.newHashSet("punct"), new HashSet<>()));
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> depContexts = filteredContexts(0);
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> childModContexts = filteredContexts(1);
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> childSkipModContexts = filteredContexts(2);

        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> rootPath = Collections.singletonList(
                new RootPathContextFactory());
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> head = Collections.singletonList(
                new RootPathContextFactory(false, 1));
        StringExtractor<DepNode> text = new LookupFeatureExtractor<>(FeatureType.Text.name());
        StringExtractor<DepNode> pos = new LookupFeatureExtractor<>(FeatureType.Pos.name());
        StringExtractor<DepNode> lemma = new LookupFeatureExtractor<>(FeatureType.Lemma.name());
        StringExtractor<DepNode> dep = new LookupFeatureExtractor<>(FeatureType.Dep.name());

        NlpFeaturePipelineFactory<NlpFocus<DepNode, DepTree>, DepNode> featureFunctionFactory
                = new NlpFeaturePipelineFactory<>(0);

        featureFunctionFactory.addBias();

        featureFunctionFactory.addFeatureFunctionFactory(windowUnigrams, text, true)
                .addFeatureFunctionFactory(windowUnigrams, pos, true)
                .addFeatureFunctionFactory(windowUnigrams, lemma, true)
                .addFeatureFunctionFactory(windowUnigrams, dep, true);

        featureFunctionFactory.addFeatureFunctionFactory(windowBigrams, text, true)
                .addFeatureFunctionFactory(windowBigrams, pos, true)
                .addFeatureFunctionFactory(windowBigrams, lemma, true)
                .addFeatureFunctionFactory(windowBigrams, dep, true);

        StringExtractor<DepNode> textDep = new ConcatenatingFeatureExtractor<>(text, dep);
        StringExtractor<DepNode> posDep = new ConcatenatingFeatureExtractor<>(pos, dep);
        StringExtractor<DepNode> lemmaDep = new ConcatenatingFeatureExtractor<>(lemma, dep);

        featureFunctionFactory.addFeatureFunctionFactory(allDeps, textDep, true)
                .addFeatureFunctionFactory(allDeps, posDep, true)
                .addFeatureFunctionFactory(allDeps, lemmaDep, true)

                .addFeatureFunctionFactory(rootPath, pos, true)
                .addFeatureFunctionFactory(rootPath, dep, true)
                .addFeatureFunctionFactory(rootPath, lemma, true)
                .addFeatureFunctionFactory(head, pos, true)
                .addFeatureFunctionFactory(head, dep, true)
                .addFeatureFunctionFactory(head, lemma, true);

        StringListExtractor<DepNode> brown = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(BROWN), dep);
        StringListExtractor<DepNode> cluster100 = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(CLUSTERS.get(0)), dep);
        StringListExtractor<DepNode> cluster320 = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(CLUSTERS.get(1)), dep);
        StringListExtractor<DepNode> cluster1000 = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(CLUSTERS.get(2)), dep);
        StringListExtractor<DepNode> cluster3200 = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(CLUSTERS.get(3)), dep);
        StringListExtractor<DepNode> cluster10000 = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(CLUSTERS.get(4)), dep);
        StringListExtractor<DepNode> wn = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(WordNetResource.WN_KEY), dep);
        StringListExtractor<DepNode> ddn = new ListConcatenatingFeatureExtractor<>(
                new ListLookupFeatureExtractor<>(DynamicDependencyNeighborsResource.DDN_KEY), dep);

        featureFunctionFactory.addMultiFeatureFunctionFactory(depContexts, brown, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster100, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster320, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster1000, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster3200, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster10000, true)
                .addMultiFeatureFunctionFactory(depContexts, wn, true)
                .addMultiFeatureFunctionFactory(depContexts, ddn, true)

                .addFeatureFunctionFactory(childModContexts, posDep, true)
                .addFeatureFunctionFactory(childModContexts, dep, true)
                .addFeatureFunctionFactory(childSkipModContexts, posDep, true)
                .addFeatureFunctionFactory(childSkipModContexts, dep, true);
        return featureFunctionFactory;
    }

    public static List<Annotator<NlpFocus<DepNode, DepTree>>> annotators() {
        List<Annotator<NlpFocus<DepNode, DepTree>>> annotators = new ArrayList<>();
        for (String cluster : CLUSTERS) {
            annotators.add(new ListAnnotator<>(cluster, new StringFunctionExtractor<>(
                    new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction())));
        }
        annotators.add(new ListAnnotator<>(BROWN, new LookupFeatureExtractor<>(FeatureType.Text.name())));
        annotators.add(new DepNodeListAnnotator<>("WN"));
        annotators.add(new DepNodeListAnnotator<>(DynamicDependencyNeighborsResource.DDN_KEY,
                new DepChildrenContextFactory("dobj", "nmod", "xcomp", "advmod")));
        annotators.add(new ListAnnotator<>("bin", new StringFunctionExtractor<>(
                new LookupFeatureExtractor<>(FeatureType.Text.name()), new LowercaseFunction())));
        return annotators;
    }

    static FeatureResourceManager resourceManager() {
        FeatureResourceManager resourceManager = new DefaultFeatureResourceManager();
        for (String cluster : CLUSTERS) {
            resourceManager.registerInitializer(cluster, new DefaultTsvResourceInitializer<>(cluster, CLUSTER_PATH + cluster)
                    .keyFunction(new LowercaseFunction()));
        }
        resourceManager.registerInitializer("brown", new BrownClusterResourceInitializer<>("brown", BWC_PATH));
        resourceManager.registerInitializer(WordNetResource.WN_KEY, WordNetResource::new);
//        resourceManager.registerInitializer(KEY, new DdnResourceInitializer(new File("data/learningResources/ddnIndex")));
        resourceManager.registerInitializer(DDN_KEY,
                new DefaultTsvResourceInitializer<DepNode>(DDN_KEY, "data/learningResources/ddnObjects.txt")
                        .mappingFunction(new LookupFeatureExtractor<>(FeatureType.Lemma.name()))
        );
        resourceManager.registerInitializer("bin", new DefaultTsvResourceInitializer<>("bin",
                "data/learningResources/counter-fitted-vectors.bin.txt"));
        return resourceManager;
    }

    public static void main(String[] args) throws Throwable {
        List<NlpFocus<DepNode, DepTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        AggregateAnnotator<NlpFocus<DepNode, DepTree>> annotator
                = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        instances.forEach(annotator::annotate);

        CrossValidation<NlpFocus<DepNode, DepTree>> cv = new CrossValidation<>(
                (NlpFocus<DepNode, DepTree> i) -> i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<NlpFocus<DepNode, DepTree>>> folds = cv.createFolds(instances, 5);

        FeaturePipelineFactory<NlpFocus<DepNode, DepTree>> factory = getFactory();
        MultiClassifier<NlpFocus<DepNode, DepTree>, String> multi = new MultiClassifier<>(
                (Serializable & Function<NlpFocus<DepNode, DepTree>, String>)
                        (i) -> i.focus().feature(FeatureType.Predicate.name()),
                (Serializable & Supplier<Classifier<NlpFocus<DepNode, DepTree>, String>>)
                        () -> new MetaModelTrainer<>(factory, LibLinearClassifier::new));
        WordSenseClassifier classifier = new WordSenseClassifier(multi, new CountingSenseInventory(), new LemmaDictionary());
        classifier.train(instances, new ArrayList<>());
        classifier.save(new ObjectOutputStream(new FileOutputStream("data/model.bin")));
        Evaluation overall = new Evaluation(cv.crossValidate(classifier, folds));
        log.info("\n{}", overall.toString());
    }
}
