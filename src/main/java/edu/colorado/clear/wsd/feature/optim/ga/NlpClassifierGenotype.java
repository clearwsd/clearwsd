package edu.colorado.clear.wsd.feature.optim.ga;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.classifier.ClassifierFactory;
import edu.colorado.clear.wsd.classifier.SparseClassifier;
import edu.colorado.clear.wsd.feature.function.AggregateFeatureFunction;
import edu.colorado.clear.wsd.feature.function.BiasFeatureFunction;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.pipeline.DefaultFeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.NlpClassifier;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Genotype for NLP classifiers.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class NlpClassifierGenotype<T extends NlpInstance, C extends SparseClassifier> implements Genotype<NlpClassifier<T>> {

    @Setter
    private double fitness = -Double.MAX_VALUE;

    private Chromosome<OptionGene<FeatureFunction<T>>> features;
    private Chromosome<OptionGene<Properties>> hyperparams;
    private ClassifierFactory<C> classifierFactory;

    public NlpClassifierGenotype(Chromosome<OptionGene<FeatureFunction<T>>> features,
                                 Chromosome<OptionGene<Properties>> hyperparams,
                                 ClassifierFactory<C> classifierFactory) {
        this.features = features;
        this.hyperparams = hyperparams;
        this.classifierFactory = classifierFactory;
    }

    @Override
    public Genotype<NlpClassifier<T>> copy() {
        NlpClassifierGenotype<T, C> geno = new NlpClassifierGenotype<>(features.copy(), hyperparams.copy(), classifierFactory);
        geno.fitness = fitness;
        return geno;
    }

    @Override
    public List<Chromosome> chromosomes() {
        return Arrays.asList(features, hyperparams);
    }

    @Override
    public void cross(Genotype<NlpClassifier<T>> other) {
        NlpClassifierGenotype<T, C> otherGenotype = (NlpClassifierGenotype<T, C>) other;
        features.cross(otherGenotype.features);
        hyperparams.cross(otherGenotype.hyperparams);
    }

    @Override
    public NlpClassifier<T> phenotype() {
        AggregateFeatureFunction<T> result = new AggregateFeatureFunction<>(features.genes().stream()
                .filter(OptionGene::active)
                .map(OptionGene::currentValue).collect(Collectors.toList()));
        result.add(new BiasFeatureFunction<>());
        C sparseClassifier = classifierFactory.create();
        Properties all = new Properties();
        hyperparams.genes().stream()
                .filter(OptionGene::active)
                .map(OptionGene::currentValue)
                .forEach(p -> p.forEach(all::put));
        sparseClassifier.initialize(all);
        FeaturePipeline<T> featurePipeline = new DefaultFeaturePipeline<>(result);
        return new NlpClassifier<>(sparseClassifier, featurePipeline);
    }

}
