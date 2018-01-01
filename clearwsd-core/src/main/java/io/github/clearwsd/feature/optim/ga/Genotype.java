/*
 * Copyright 2017 James Gung
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

package io.github.clearwsd.feature.optim.ga;

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
     *
     * @param other other parent to be crossed with this one
     */
    void cross(Genotype<T> other);

    /**
     * Return the phenotype/gene expression for this genotype.
     */
    T phenotype();

}
