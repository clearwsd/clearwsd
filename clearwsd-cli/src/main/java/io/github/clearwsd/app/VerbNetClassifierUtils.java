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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import io.github.clearwsd.feature.context.RootPathContextFactory;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.feature.extractor.string.LowercaseFunction;
import io.github.clearwsd.feature.optim.FeatureFunctionFactory;
import io.github.clearwsd.feature.optim.MetaModelTrainer;
import io.github.clearwsd.feature.optim.NlpFeaturePipelineFactory;
import io.github.clearwsd.feature.resource.BrownClusterResourceInitializer;
import io.github.clearwsd.feature.resource.DefaultFeatureResourceManager;
import io.github.clearwsd.feature.resource.DefaultTsvResourceInitializer;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import io.github.clearwsd.feature.resource.WordNetResource.WordNetInitializer;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.utils.CountingSenseInventory;
import io.github.clearwsd.utils.LemmaDictionary;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.feature.context.Contexts.excludingDeps;
import static io.github.clearwsd.feature.context.Contexts.head;
import static io.github.clearwsd.feature.context.Contexts.window;
import static io.github.clearwsd.feature.extractor.Extractors.concat;
import static io.github.clearwsd.feature.extractor.Extractors.form;
import static io.github.clearwsd.feature.extractor.Extractors.lemma;
import static io.github.clearwsd.feature.extractor.Extractors.listConcat;
import static io.github.clearwsd.feature.extractor.Extractors.listLookup;
import static io.github.clearwsd.feature.extractor.Extractors.lookup;
import static io.github.clearwsd.feature.extractor.Extractors.lowerForm;
import static io.github.clearwsd.feature.extractor.Extractors.lowerLemma;
import static io.github.clearwsd.feature.resource.BrownClusterResourceInitializer.BWC_KEY;
import static io.github.clearwsd.feature.resource.DynamicDependencyNeighborsResource.DDN_KEY;
import static io.github.clearwsd.feature.resource.WordNetResource.WN_KEY;
import static io.github.clearwsd.type.FeatureType.Dep;
import static io.github.clearwsd.type.FeatureType.Gold;
import static io.github.clearwsd.type.FeatureType.Pos;
import static java.util.Collections.singletonList;

/**
 * Performs a random search for the best configuration with a performance metric and set of parameters.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetClassifierUtils {

    private static final String BASE_PATH = "features/verbnet/";
    private static final String CLUSTER_PATH = BASE_PATH + "clusters/";
    private static final String BWC_PATH = BASE_PATH + "BWC.tsv";
    private static final String DDN_PATH = BASE_PATH + "DDN.tsv";

    private static List<String> clusters = Lists.newArrayList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200",
            "cluster-10000");

    static List<String> CLUSTERS
            = Arrays.asList("cluster-100", "cluster-320", "cluster-1000", "cluster-3200", "cluster-10000");

    static String BROWN = "brown";

    static List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowUnigrams() {
        return Arrays.asList(
                window(0),
                window(-1, 0),
                window(0, 1),
                window(-1, 1),
                window(-1, 0, 1),
                window(-2, -1, 0, 1),
                window(-1, 0, 1, 2),
                window(-2, -1, 1, 2),
                window(-2, -1, 0, 1, 2)
        );
    }

    static List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> collocations() {
        return Arrays.asList(
                new CompositeContextFactory<>(
                        window(true, -2, -1),
                        window(true, 1, 2),
                        window(true, -1, 0, 1),
                        window(true, -3, -2, -1),
                        window(true, -2, -1, 0, 1),
                        window(true, -1, 0, 1, 2),
                        window(true, 1, 2, 3)
                ),
                new CompositeContextFactory<>(
                        window(true, -2, -1),
                        window(true, 1, 2),
                        window(true, -1, 0, 1),
                        window(true, -3, -2, -1),
                        window(true, 1, 2, 3)
                ),
                new CompositeContextFactory<>(
                        window(true, -2, -1),
                        window(true, 1, 2),
                        window(true, -1, 1)
                ),
                new CompositeContextFactory<>(
                        window(true, -2, -1),
                        window(true, 1, 2)
                ),
                new CompositeContextFactory<>(
                        window(true, -1, 0),
                        window(true, 0, 1)
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

    private static FeatureFunctionFactory<NlpFocus<DepNode, DepTree>> getFactory() {
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowUnigrams = windowUnigrams();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowBigrams = collocations();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> allDeps =
                singletonList(excludingDeps(Sets.newHashSet("punct")));
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> depContexts = filteredContexts(0);

        NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> rootPath = new RootPathContextFactory();
        NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> head = head();

        StringExtractor<DepNode> text = lowerForm();
        StringExtractor<DepNode> lemma = lowerLemma();
        StringExtractor<DepNode> dep = lookup(Dep);
        StringExtractor<DepNode> pos = lookup(Pos);

        NlpFeaturePipelineFactory<NlpFocus<DepNode, DepTree>, DepNode> featureFunctionFactory = new NlpFeaturePipelineFactory<>(0);

        featureFunctionFactory.addBias();

        featureFunctionFactory.addFeatureFunctionFactory(windowUnigrams, text, true)
                .addFeatureFunctionFactory(windowUnigrams, pos, true)
                .addFeatureFunctionFactory(windowUnigrams, lemma, true)
                .addFeatureFunctionFactory(windowUnigrams, dep, true);

        featureFunctionFactory.addFeatureFunctionFactory(windowBigrams, text, true)
                .addFeatureFunctionFactory(windowBigrams, pos, true)
                .addFeatureFunctionFactory(windowBigrams, lemma, true)
                .addFeatureFunctionFactory(windowBigrams, dep, true);

        StringExtractor<DepNode> textDep = concat(text, dep);
        StringExtractor<DepNode> posDep = concat(pos, dep);
        StringExtractor<DepNode> lemmaDep = concat(lemma, dep);

        featureFunctionFactory.addFeatureFunctionFactory(allDeps, textDep, true)
                .addFeatureFunctionFactory(allDeps, posDep, true)
                .addFeatureFunctionFactory(allDeps, lemmaDep, true)

                .addFeatureFunctionFactory(rootPath, pos, true)
                .addFeatureFunctionFactory(rootPath, dep, true)
                .addFeatureFunctionFactory(rootPath, lemma, true)
                .addFeatureFunctionFactory(head, pos, true)
                .addFeatureFunctionFactory(head, dep, true)
                .addFeatureFunctionFactory(head, lemma, true);

        StringListExtractor<DepNode> wn = listConcat(listLookup(WN_KEY), dep);
        StringListExtractor<DepNode> ddn = listConcat(listLookup(DDN_KEY), dep);
        StringListExtractor<DepNode> brown = listConcat(listLookup(BROWN), dep);
        StringListExtractor<DepNode> cluster100 = listConcat(listLookup(CLUSTERS.get(0)), dep);
        StringListExtractor<DepNode> cluster320 = listConcat(listLookup(CLUSTERS.get(1)), dep);
        StringListExtractor<DepNode> cluster1000 = listConcat(listLookup(CLUSTERS.get(2)), dep);
        StringListExtractor<DepNode> cluster3200 = listConcat(listLookup(CLUSTERS.get(3)), dep);
        StringListExtractor<DepNode> cluster10000 = listConcat(listLookup(CLUSTERS.get(4)), dep);

        featureFunctionFactory
                .addMultiFeatureFunctionFactory(depContexts, wn, true)
                .addMultiFeatureFunctionFactory(depContexts, ddn, true)
                .addMultiFeatureFunctionFactory(depContexts, brown, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster100, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster320, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster1000, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster3200, true)
                .addMultiFeatureFunctionFactory(depContexts, cluster10000, true);
        return featureFunctionFactory;
    }

    public static List<Annotator<NlpFocus<DepNode, DepTree>>> annotators() {
        List<Annotator<NlpFocus<DepNode, DepTree>>> annotators = new ArrayList<>();
        for (String cluster : clusters) {
            annotators.add(new ListAnnotator<>(cluster, lowerForm()));
        }
        annotators.add(new ListAnnotator<>(BWC_KEY, form()));
        annotators.add(new DepNodeListAnnotator<>(DDN_KEY, new DepChildrenContextFactory("dobj")));
        annotators.add(new DepNodeListAnnotator<>(WN_KEY));
        return annotators;
    }

    static FeatureResourceManager resourceManager() {
        FeatureResourceManager resources = new DefaultFeatureResourceManager();
        resources.registerInitializer(WN_KEY, new WordNetInitializer<>());
        resources.registerInitializer(DDN_KEY, new DefaultTsvResourceInitializer<DepNode>(DDN_KEY, getURL(DDN_PATH))
                .mappingFunction(lemma()));
        resources.registerInitializer(BWC_KEY, new BrownClusterResourceInitializer<>(BWC_KEY, getURL(BWC_PATH)));
        for (String cluster : clusters) {
            resources.registerInitializer(cluster, new DefaultTsvResourceInitializer<>(cluster, getURL(CLUSTER_PATH + cluster))
                    .keyFunction(new LowercaseFunction()));
        }
        return resources;
    }

    private static URL getURL(String path) {
        return VerbNetClassifierUtils.class.getClassLoader().getResource(path);
    }

    public static void main(String[] args) throws Throwable {
        List<NlpFocus<DepNode, DepTree>> instances = new VerbNetReader().readInstances(new FileInputStream(args[0]));
        List<NlpFocus<DepNode, DepTree>> devInstances = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        List<NlpFocus<DepNode, DepTree>> testInstances = new VerbNetReader().readInstances(new FileInputStream(args[2]));

        AggregateAnnotator<NlpFocus<DepNode, DepTree>> annotator = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        instances.forEach(annotator::annotate);
        devInstances.forEach(annotator::annotate);
        testInstances.forEach(annotator::annotate);

        FeatureFunctionFactory<NlpFocus<DepNode, DepTree>> factory = getFactory();
        MultiClassifier<NlpFocus<DepNode, DepTree>, String> multi = new MultiClassifier<>(
                (Serializable & Function<NlpFocus<DepNode, DepTree>, String>)
                        (i) -> i.focus().feature(FeatureType.Predicate.name()),
                (Serializable & Supplier<Classifier<NlpFocus<DepNode, DepTree>, String>>)
                        () -> new MetaModelTrainer<>(factory, LibLinearClassifier::new));
        WordSenseClassifier classifier = new WordSenseClassifier(multi, new CountingSenseInventory(), new LemmaDictionary());

        CrossValidation<NlpFocus<DepNode, DepTree>> cv = new CrossValidation<>(
                (NlpFocus<DepNode, DepTree> i) -> i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<NlpFocus<DepNode, DepTree>>> folds = cv.createFolds(instances, 5, 0.8);

        Evaluation overall = new Evaluation(cv.crossValidate(classifier, folds));
        log.info("\n{}", overall.toString());

        classifier.train(instances, devInstances);
        Evaluation evaluation = new Evaluation();
        for (NlpFocus<DepNode, DepTree> instance : devInstances) {
            evaluation.add(classifier.classify(instance), instance.feature(Gold));
        }
        log.info("\n{}", evaluation.toString());
        evaluation = new Evaluation();
        for (NlpFocus<DepNode, DepTree> instance : testInstances) {
            evaluation.add(classifier.classify(instance), instance.feature(Gold));
        }
        log.info("\n{}", evaluation.toString());

    }
}
