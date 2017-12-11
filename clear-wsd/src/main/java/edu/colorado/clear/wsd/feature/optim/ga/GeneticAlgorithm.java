package edu.colorado.clear.wsd.feature.optim.ga;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jamesgung
 */
@Slf4j
@Getter
@Accessors(fluent = true)
public class GeneticAlgorithm<T> {

    private Random random;

    @Setter
    private double activationProbability = 0.75;
    @Setter
    private int size = 100;
    @Setter
    private int maxEpochs = 50;
    @Setter
    private int patience = 10;
    @Setter
    private double maxFitness = 1.0;
    @Setter
    private double baseMutationProbability = 0.2;
    @Setter
    private double baseCrossoverProbability = 0.9;
    @Setter
    private int numElites = 1;

    private int epoch;
    private int epochsNoChange;
    private double overallBest;
    private double averageFitness;

    private Genotype<T> best;
    private List<Genotype<T>> elites;
    private List<Genotype<T>> population;

    @Setter
    private Genotype<T> prototype;
    @Setter
    private Function<T, Double> fitnessFunction;

    public GeneticAlgorithm(int seed, Function<T, Double> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
        this.random = new Random(seed);
    }

    private List<Genotype<T>> initialize(int size) {
        List<Genotype<T>> population = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            Genotype<T> genotype = prototype.copy();
            genotype.chromosomes().forEach(Chromosome::randomize);
            population.add(genotype);
        }
        return population;
    }

    private void computeFitness() {
        population.parallelStream().forEach(c -> c.fitness(fitnessFunction.apply(c.phenotype())));
        averageFitness = population.stream().map(Genotype::fitness)
                .mapToDouble(i -> i)
                .average().orElse(0);
        population.sort(Comparator.comparingDouble(Genotype::fitness));
        best = population.get(population.size() - 1);
    }

    private List<Genotype<T>> next() {
        if (epoch == 0) {
            return initialize(size);
        }
        List<Genotype<T>> newPopulation = new ArrayList<>();

        // add cross-over population
        while (newPopulation.size() < size - numElites - 1) {
            List<Genotype<T>> children = cross(select(), select());
            children.stream().map(this::mutate).forEach(newPopulation::add);
        }
        // if not even, add single mutated selection
        if (newPopulation.size() < size - numElites) {
            newPopulation.add(mutate(select()));
        }
        // add elites
        for (int i = 0; i < numElites; ++i) {
            newPopulation.add(elites.get(i).copy());
        }
        return newPopulation;
    }

    private Genotype<T> select() {
        double total = size * (1 + size) / 2;
        double rand = random.nextDouble();
        double probability = 0;
        int index = 1;
        for (Genotype<T> individual : population) {
            probability += index++ / total;
            if (rand < probability) {
                return individual;
            }
        }
        return population.get(population.size() - 1);
    }

    private List<Genotype<T>> cross(Genotype<T> first, Genotype<T> second) {
        Genotype<T> firstCopy = first.copy();
        Genotype<T> secondCopy = second.copy();
        if (random.nextDouble() < baseCrossoverProbability) {
            firstCopy.cross(secondCopy);
            firstCopy.fitness(averageFitness);
            secondCopy.fitness(averageFitness);
        }
        return Arrays.asList(firstCopy, secondCopy);
    }

    private Genotype<T> mutate(Genotype<T> individual) {
        individual = individual.copy();
        double fitness = individual.fitness();
        double mutationProbability = fitness <= averageFitness ? baseMutationProbability
                : baseMutationProbability * (maxFitness - fitness) / (maxFitness - averageFitness);
        for (Chromosome<?> chromosome : individual.chromosomes()) {
            for (Gene gene : chromosome.genes()) {
                if (random.nextDouble() < mutationProbability) {
                    gene.mutate();
                }
            }
        }
        return individual;
    }

    public void run() {
        overallBest = -Double.MAX_VALUE;
        averageFitness = -Double.MAX_VALUE;
        for (epoch = 0, epochsNoChange = 0; epoch < maxEpochs && (patience <= 0 || epochsNoChange < patience); ++epoch) {
            Stopwatch sw = Stopwatch.createStarted();
            population = next();
            computeFitness();

            if (best.fitness() > overallBest) {
                overallBest = best.fitness();
                elites = Lists.reverse(population).subList(0, numElites);
                epochsNoChange = 0;
                log.debug("Epoch {} ({} epochs remain, max: {}, avg: {}, epoch time: {}", epoch,
                        patience > 0 && patience < (maxEpochs - epoch)
                                ? String.format("%d or %d", Math.max(0, maxEpochs - epoch),
                                Math.max(0, patience - epochsNoChange)) : Math.max(0, maxEpochs - epoch),
                        new DecimalFormat("#.####").format(overallBest),
                        new DecimalFormat("#.####").format(averageFitness), sw);
            } else {
                ++epochsNoChange;
            }
        }
    }

}
