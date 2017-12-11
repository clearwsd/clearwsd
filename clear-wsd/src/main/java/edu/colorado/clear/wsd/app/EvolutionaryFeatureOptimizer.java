package edu.colorado.clear.wsd.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.classifier.PaClassifier;
import edu.colorado.clear.wsd.corpus.VerbNetReader;
import edu.colorado.clear.wsd.eval.CrossValidation;
import edu.colorado.clear.wsd.eval.Evaluation;
import edu.colorado.clear.wsd.feature.annotator.AggregateAnnotator;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.context.RootPathContextFactory;
import edu.colorado.clear.wsd.feature.extractor.ConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.ListConcatenatingFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.LookupFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.NlpFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringListLookupFeature;
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
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.app.VerbNetClassifierTrainer.resourceManager;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.BROWN;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.CLUSTERS;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.collocations;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.filteredContexts;
import static edu.colorado.clear.wsd.app.VerbNetClassifierUtils.windowUnigrams;

/**
 * @author jamesgung
 */
@Slf4j
public class EvolutionaryFeatureOptimizer {

    private static OptionGene<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> gene(
            List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> featureFunctions,
            GeneticAlgorithm ga) {
        return new OptionGene<>(featureFunctions, ga.random(), ga.activationProbability());
    }

    private static List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> getFeatureFunctions(
            List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> contexts,
            NlpFeatureExtractor<DepNode, String> extractor) {
        List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> results = new ArrayList<>();
        for (NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> factory : contexts) {
            results.add(new StringFeatureFunction<>(factory, Collections.singletonList(extractor)));
        }
        return results;
    }

    private static List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> getListFeatureFunctions(
            List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> contexts,
            NlpFeatureExtractor<DepNode, List<String>> extractor) {
        List<FeatureFunction<FocusInstance<DepNode, DependencyTree>>> results = new ArrayList<>();
        for (NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode> factory : contexts) {
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

    private static Chromosome<OptionGene<FeatureFunction<FocusInstance<DepNode, DependencyTree>>>> chromosome(GeneticAlgorithm ga) {

        List<OptionGene<FeatureFunction<FocusInstance<DepNode, DependencyTree>>>> genes = new ArrayList<>();
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> windowUnigrams = windowUnigrams();
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> windowBigrams = collocations();
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> depContexts = filteredContexts(0);
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> childModContexts = filteredContexts(1);
        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> childSkipModContexts = filteredContexts(2);

        List<NlpContextFactory<FocusInstance<DepNode, DependencyTree>, DepNode>> rootPath = Collections.singletonList(
                new RootPathContextFactory());

        NlpFeatureExtractor<DepNode, String> text = new LookupFeatureExtractor<>(FeatureType.Text.name());
        NlpFeatureExtractor<DepNode, String> pos = new LookupFeatureExtractor<>(FeatureType.Pos.name());
        NlpFeatureExtractor<DepNode, String> lemma = new LookupFeatureExtractor<>(FeatureType.Lemma.name());
        NlpFeatureExtractor<DepNode, String> dep = new LookupFeatureExtractor<>(FeatureType.Dep.name());

        NlpFeatureExtractor<DepNode, String> textDep = new ConcatenatingFeatureExtractor<>(text, dep);
        NlpFeatureExtractor<DepNode, String> posDep = new ConcatenatingFeatureExtractor<>(pos, dep);
        NlpFeatureExtractor<DepNode, String> lemmaDep = new ConcatenatingFeatureExtractor<>(lemma, dep);

        NlpFeatureExtractor<DepNode, List<String>> brown = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(BROWN), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster100 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(0)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster320 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(1)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster1000 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(2)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster3200 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(3)), dep);
        NlpFeatureExtractor<DepNode, List<String>> cluster10000 = new ListConcatenatingFeatureExtractor<>(
                new StringListLookupFeature<>(CLUSTERS.get(4)), dep);

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
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));
        AggregateAnnotator<FocusInstance<DepNode, DependencyTree>> annotator
                = new AggregateAnnotator<>(VerbNetClassifierUtils.annotators());
        annotator.initialize(resourceManager());
        instances.forEach(annotator::annotate);

        CrossValidation<FocusInstance<DepNode, DependencyTree>> cv = new CrossValidation<>(
                (FocusInstance<DepNode, DependencyTree> i) -> i.feature(FeatureType.Gold));
        List<CrossValidation.Fold<FocusInstance<DepNode, DependencyTree>>> folds = cv.createFolds(instances, 5);

        CrossValidatingFitnessFunction<FocusInstance<DepNode, DependencyTree>> fitness
                = new CrossValidatingFitnessFunction<>(cv);

        GeneticAlgorithm<NlpClassifier<FocusInstance<DepNode, DependencyTree>>> ga = new GeneticAlgorithm<>(0, fitness);
        Genotype<NlpClassifier<FocusInstance<DepNode, DependencyTree>>> genotype =
                new NlpClassifierGenotype<>(chromosome(ga), hyperparams(ga), PaClassifier::new);
        ga.prototype(genotype);
        EvolutionaryModelTrainer<FocusInstance<DepNode, DependencyTree>> modelTrainer
                = new EvolutionaryModelTrainer<>(ga);
        List<Evaluation> evaluations = cv.crossValidate(modelTrainer, folds);
        for (Evaluation evaluation : evaluations) {
            log.debug("\n{}", evaluation.toString());
        }
        log.debug("\n\n{}", new Evaluation(evaluations));
    }

}
