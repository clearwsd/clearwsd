package edu.colorado.clear.wsd.feature.optim.ga;

import java.util.List;

/**
 * Genotype/individual in a population. Can contain multiple chromosomes.
 *
 * @param <T> phenotype/result of gene expression
 * @author jamesgung
 */
public interface Genotype<T> {

    /**
     * Current fitness of this genotype.
     */
    double fitness();

    /**
     * Set the fitness of this genotype to a particular value.
     *
     * @param fitness fitness value
     */
    void fitness(double fitness);

    /**
     * Return a list of all chromosomes associated with this genotype.
     */
    List<Chromosome> chromosomes();

    /**
     * Return a copy of this genotype.
     */
    Genotype<T> copy();

    /**
     * Perform crossover with this genotype and another parent.
     */
    void cross(Genotype<T> other);

    /**
     * Return the phenotype/gene expression for this genotype.
     */
    T phenotype();

}
