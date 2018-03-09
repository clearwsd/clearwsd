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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import io.github.clearwsd.classifier.PaClassifier;
import io.github.clearwsd.corpus.semlink.VerbNetReader;
import io.github.clearwsd.eval.CrossValidation;
import io.github.clearwsd.eval.Evaluation;
import io.github.clearwsd.feature.annotator.AggregateAnnotator;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.feature.function.FeatureFunction;
import io.github.clearwsd.feature.function.MultiStringFeatureFunction;
import io.github.clearwsd.feature.function.StringFeatureFunction;
import io.github.clearwsd.feature.optim.EvolutionaryModelTrainer;
import io.github.clearwsd.feature.optim.ga.Chromosome;
import io.github.clearwsd.feature.optim.ga.CrossValidatingFitnessFunction;
import io.github.clearwsd.feature.optim.ga.DefaultChromosome;
import io.github.clearwsd.feature.optim.ga.GeneticAlgorithm;
import io.github.clearwsd.feature.optim.ga.Genotype;
import io.github.clearwsd.feature.optim.ga.NlpClassifierGenotype;
import io.github.clearwsd.feature.optim.ga.OptionGene;
import io.github.clearwsd.feature.pipeline.NlpClassifier;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.app.VerbNetClassifierUtils.BROWN;
import static io.github.clearwsd.app.VerbNetClassifierUtils.CLUSTERS;
import static io.github.clearwsd.app.VerbNetClassifierUtils.collocations;
import static io.github.clearwsd.app.VerbNetClassifierUtils.filteredContexts;
import static io.github.clearwsd.app.VerbNetClassifierUtils.windowUnigrams;
import static io.github.clearwsd.feature.context.Contexts.focus;
import static io.github.clearwsd.feature.context.Contexts.head;
import static io.github.clearwsd.feature.extractor.Extractors.concat;
import static io.github.clearwsd.feature.extractor.Extractors.listConcat;
import static io.github.clearwsd.feature.extractor.Extractors.listLookup;
import static io.github.clearwsd.feature.extractor.Extractors.lookup;
import static io.github.clearwsd.feature.extractor.Extractors.lowerForm;
import static io.github.clearwsd.feature.extractor.Extractors.lowerLemma;
import static io.github.clearwsd.type.FeatureType.Dep;
import static io.github.clearwsd.type.FeatureType.Pos;

/**
 * @author jamesgung
 */
@Slf4j
public class EvolutionaryFeatureOptimizer {

    private static OptionGene<FeatureFunction<NlpFocus<DepNode, DepTree>>> gene(
            List<FeatureFunction<NlpFocus<DepNode, DepTree>>> featureFunctions,
            GeneticAlgorithm ga) {
        return new OptionGene<>(featureFunctions, ga.random(), ga.activationProbability());
    }

    private static List<FeatureFunction<NlpFocus<DepNode, DepTree>>> getFeatureFunctions(
            List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> contexts,
            StringExtractor<DepNode> extractor) {
        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> results = new ArrayList<>();
        for (NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> factory : contexts) {
            results.add(new StringFeatureFunction<>(factory, Collections.singletonList(extractor)));
        }
        return results;
    }

    private static List<FeatureFunction<NlpFocus<DepNode, DepTree>>> getListFeatureFunctions(
            List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> contexts,
            StringListExtractor<DepNode> extractor) {
        List<FeatureFunction<NlpFocus<DepNode, DepTree>>> results = new ArrayList<>();
        for (NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode> factory : contexts) {
            results.add(new MultiStringFeatureFunction<>(factory, Collections.singletonList(extractor)));
        }
        return results;
    }

    private static OptionGene<Properties> propertyGene(GeneticAlgorithm ga, String value, String... options) {
        return new OptionGene<>(Arrays.stream(options).map(o -> {
            Properties properties = new Properties();
            properties.setProperty(value, o);
            return properties;
        }).collect(Collectors.toList()), ga.random(), 1);
    }

    private static Chromosome<OptionGene<Properties>> hyperparams(GeneticAlgorithm ga) {
        List<OptionGene<Properties>> genes = new ArrayList<>();
        genes.add(propertyGene(ga, "Cost", "10", "1", "0.1"));
        genes.add(propertyGene(ga, "Epsilon", "0.1", "0.01", "0.001"));
        return new DefaultChromosome<>(genes, ga.random());
    }

    private static Chromosome<OptionGene<FeatureFunction<NlpFocus<DepNode, DepTree>>>> chromosome(GeneticAlgorithm ga) {

        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowUnigrams = windowUnigrams();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowBigrams = collocations();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> depContexts = filteredContexts(0);
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> childModContexts = filteredContexts(1);
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> childSkipModContexts = filteredContexts(2);

        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> head = Collections.singletonList(head());
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> focus = Collections.singletonList(focus());

        StringExtractor<DepNode> text = lowerForm();
        StringExtractor<DepNode> lemma = lowerLemma();
        StringExtractor<DepNode> dep = lookup(Dep);
        StringExtractor<DepNode> pos = lookup(Pos);

        StringExtractor<DepNode> textDep = concat(text, dep);
        StringExtractor<DepNode> posDep = concat(pos, dep);
        StringExtractor<DepNode> lemmaDep = concat(lemma, dep);

        StringListExtractor<DepNode> brown = listConcat(listLookup(BROWN), dep);
        StringListExtractor<DepNode> cluster100 = listConcat(listLookup(CLUSTERS.get(0)), dep);
        StringListExtractor<DepNode> cluster320 = listConcat(listLookup(CLUSTERS.get(1)), dep);
        StringListExtractor<DepNode> cluster1000 = listConcat(listLookup(CLUSTERS.get(2)), dep);
        StringListExtractor<DepNode> cluster3200 = listConcat(listLookup(CLUSTERS.get(3)), dep);
        StringListExtractor<DepNode> cluster10000 = listConcat(listLookup(CLUSTERS.get(4)), dep);

        List<OptionGene<FeatureFunction<NlpFocus<DepNode, DepTree>>>> genes = new ArrayList<>();
        genes.add(gene(getFeatureFunctions(windowUnigrams, text), ga));
        genes.add(gene(getFeatureFunctions(windowUnigrams, pos), ga));
        genes.add(gene(getFeatureFunctions(windowUnigrams, lemma), ga));
        genes.add(gene(getFeatureFunctions(windowUnigrams, dep), ga));

        genes.add(gene(getFeatureFunctions(windowBigrams, text), ga));
        genes.add(gene(getFeatureFunctions(windowBigrams, pos), ga));
        genes.add(gene(getFeatureFunctions(windowBigrams, lemma), ga));
        genes.add(gene(getFeatureFunctions(windowBigrams, dep), ga));

        genes.add(gene(getFeatureFunctions(depContexts, textDep), ga));
        genes.add(gene(getFeatureFunctions(depContexts, posDep), ga));
        genes.add(gene(getFeatureFunctions(depContexts, lemmaDep), ga));

        genes.add(gene(getFeatureFunctions(head, textDep), ga));
        genes.add(gene(getFeatureFunctions(head, posDep), ga));
        genes.add(gene(getFeatureFunctions(head, lemmaDep), ga));

        genes.add(gene(getFeatureFunctions(childModContexts, posDep), ga));
        genes.add(gene(getFeatureFunctions(childModContexts, dep), ga));
        genes.add(gene(getFeatureFunctions(childSkipModContexts, posDep), ga));
        genes.add(gene(getFeatureFunctions(childSkipModContexts, dep), ga));

        genes.add(gene(getListFeatureFunctions(depContexts, brown), ga));
        genes.add(gene(getListFeatureFunctions(depContexts, cluster100), ga));
        genes.add(gene(getListFeatureFunctions(depContexts, cluster320), ga));
        genes.add(gene(getListFeatureFunctions(depContexts, cluster1000), ga));
        genes.add(gene(getListFeatureFunctions(depContexts, cluster3200), ga));
        genes.add(gene(getListFeatureFunctions(depContexts, cluster10000), ga));

        genes.add(gene(getListFeatureFunctions(focus, brown), ga));
        genes.add(gene(getListFeatureFunctions(focus, cluster100), ga));
        genes.add(gene(getListFeatureFunctions(focus, cluster320), ga));
        genes.add(gene(getListFeatureFunctions(focus, cluster1000), ga));
        genes.add(gene(getListFeatureFunctions(focus, cluster3200), ga));
        genes.add(gene(getListFeatureFunctions(focus, cluster10000), ga));

        return new DefaultChromosome<>(genes, ga.random());
    }

    public static void main(String[] args) throws IOException {
        List<NlpFocus<DepNode, DepTree>> instances = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        AggregateAnnotator<NlpFocus<DepNode, DepTree>> annotator = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(VerbNetClassifierUtils.resourceManager());
        instances.forEach(annotator::annotate);

        CrossValidation<NlpFocus<DepNode, DepTree>> cv = new CrossValidation<>(
                (NlpFocus<DepNode, DepTree> i) -> i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<NlpFocus<DepNode, DepTree>>> folds = cv.createFolds(instances, 5);

        CrossValidatingFitnessFunction<NlpFocus<DepNode, DepTree>> fitness = new CrossValidatingFitnessFunction<>(cv);

        GeneticAlgorithm<NlpClassifier<NlpFocus<DepNode, DepTree>>> ga = new GeneticAlgorithm<>(0, fitness);
        Genotype<NlpClassifier<NlpFocus<DepNode, DepTree>>> genotype =
                new NlpClassifierGenotype<>(chromosome(ga), hyperparams(ga), PaClassifier::new);
        ga.prototype(genotype);
        EvolutionaryModelTrainer<NlpFocus<DepNode, DepTree>> modelTrainer = new EvolutionaryModelTrainer<>(ga);
        List<Evaluation> evaluations = cv.crossValidate(modelTrainer, folds);
        for (Evaluation evaluation : evaluations) {
            log.debug("\n{}", evaluation.toString());
        }
        log.debug("\n\n{}", new Evaluation(evaluations));
    }

}
