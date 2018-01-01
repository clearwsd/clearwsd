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
