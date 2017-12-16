package edu.colorado.clear.wsd.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.colorado.clear.type.DepNode;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.type.NlpFocus;
import edu.colorado.clear.wsd.classifier.PaClassifier;
import edu.colorado.clear.wsd.corpus.semlink.VerbNetReader;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorado.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListLookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListExtractor;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.function.MultiStringFeatureFunction;
import edu.colorado.clear.wsd.feature.function.StringFeatureFunction;
import edu.colorado.clear.wsd.feature.optim.EvolutionaryModelTrainer;
import edu.colorado.clear.wsd.feature.optim.ga.Chromosome;
import edu.colorado.clear.wsd.feature.optim.ga.CrossValidatingFitnessFunction;
import edu.colorado.clear.wsd.feature.optim.ga.DefaultChromosome;
import edu.colorado.clear.wsd.feature.optim.ga.GeneticAlgorithm;
import edu.colorado.clear.wsd.feature.optim.ga.Genotype;
import edu.colorado.clear.wsd.feature.optim.ga.NlpClassifierGenotype;
import edu.colorado.clear.wsd.feature.optim.ga.OptionGene;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.BROWN;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.CLUSTERS;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.collocations;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.filteredContexts;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.resourceManager;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.windowUnigrams;

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

        List<OptionGene<FeatureFunction<NlpFocus<DepNode, DepTree>>>> genes = new ArrayList<>();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowUnigrams = windowUnigrams();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> windowBigrams = collocations();
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> depContexts = filteredContexts(0);
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> childModContexts = filteredContexts(1);
        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> childSkipModContexts = filteredContexts(2);

        List<NlpContextFactory<NlpFocus<DepNode, DepTree>, DepNode>> rootPath = Collections.singletonList(
                new RootPathContextFactory());

        StringExtractor<DepNode> text = new LookupFeatureExtractor<>(FeatureType.Text.name());
        StringExtractor<DepNode> pos = new LookupFeatureExtractor<>(FeatureType.Pos.name());
        StringExtractor<DepNode> lemma = new LookupFeatureExtractor<>(FeatureType.Lemma.name());
        StringExtractor<DepNode> dep = new LookupFeatureExtractor<>(FeatureType.Dep.name());

        StringExtractor<DepNode> textDep = new ConcatenatingFeatureExtractor<>(text, dep);
        StringExtractor<DepNode> posDep = new ConcatenatingFeatureExtractor<>(pos, dep);
        StringExtractor<DepNode> lemmaDep = new ConcatenatingFeatureExtractor<>(lemma, dep);

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

        genes.add(gene(getFeatureFunctions(rootPath, textDep), ga));
        genes.add(gene(getFeatureFunctions(rootPath, posDep), ga));
        genes.add(gene(getFeatureFunctions(rootPath, lemmaDep), ga));

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

        return new DefaultChromosome<>(genes, ga.random());
    }

    public static void main(String[] args) throws IOException {
        List<NlpFocus<DepNode, DepTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        AggregateAnnotator<NlpFocus<DepNode, DepTree>> annotator
                = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        instances.forEach(annotator::annotate);

        CrossValidation<NlpFocus<DepNode, DepTree>> cv = new CrossValidation<>(
                (NlpFocus<DepNode, DepTree> i) -> i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<NlpFocus<DepNode, DepTree>>> folds = cv.createFolds(instances, 5);

        CrossValidatingFitnessFunction<NlpFocus<DepNode, DepTree>> fitness
                = new CrossValidatingFitnessFunction<>(cv);

        GeneticAlgorithm<NlpClassifier<NlpFocus<DepNode, DepTree>>> ga = new GeneticAlgorithm<>(0, fitness);
        Genotype<NlpClassifier<NlpFocus<DepNode, DepTree>>> genotype =
                new NlpClassifierGenotype<>(chromosome(ga), hyperparams(ga), PaClassifier::new);
        ga.prototype(genotype);
        EvolutionaryModelTrainer<NlpFocus<DepNode, DepTree>> modelTrainer
                = new EvolutionaryModelTrainer<>(ga);
        List<Evaluation> evaluations = cv.crossValidate(modelTrainer, folds);
        for (Evaluation evaluation : evaluations) {
            log.debug("\n{}", evaluation.toString());
        }
        log.debug("\n\n{}", new Evaluation(evaluations));
    }

}
