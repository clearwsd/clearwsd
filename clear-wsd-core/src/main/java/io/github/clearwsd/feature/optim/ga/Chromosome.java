package io.github.clearwsd.feature.optim.ga;

import java.util.List;

/**
 * Chromosome for use in a genetic algorithm.
 *
 * @author jamesgung
 */
public interface Chromosome<G extends Gene> {

    /**
     * Returns a copy of this chromosome.
     */
    Chromosome<G> copy();

    /**
     * Perform crossover with a target chromosome, applying changes to resulting
     *
     * @param target other parent chromosome
     */
    void cross(Chromosome<G> target);

    /**
     * Returns a list of genes for this chromosome.
     */
    List<G> genes();

    /**
     * Sets the genes of this chromosome.
     *
     * @param genes list of genes
     */
    void genes(List<G> genes);

    /**
     * Randomize the genes in this chromosome.
     */
    void randomize();

}
