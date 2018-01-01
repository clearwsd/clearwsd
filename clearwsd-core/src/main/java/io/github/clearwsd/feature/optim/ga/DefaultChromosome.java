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

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Feature chromosome.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true, chain = false)
@AllArgsConstructor
public class DefaultChromosome<G extends Gene> implements Chromosome<G> {

    private List<G> genes;
    private Random random;

    private List<G> genesCopy() {
        //noinspection unchecked
        return genes.stream().map(g -> (G) g.copy()).collect(Collectors.toList());
    }

    @Override
    public void randomize() {
        genes.forEach(Gene::mutate);
    }

    @Override
    public DefaultChromosome<G> copy() {
        return new DefaultChromosome<>(genesCopy(), random);
    }

    @Override
    public void cross(Chromosome<G> parent) {
        int size = genes.size();
        Preconditions.checkArgument(size == parent.genes().size(),
                "Number of genes must be equal (was %d vs. %d).", genes.size(), parent.genes().size());
        DefaultChromosome<G> other = (DefaultChromosome<G>) parent;
        int point = random.nextInt(size);
        List<G> newFirst = Stream.concat(
                genesCopy().subList(0, point).stream(),
                other.genesCopy().subList(point, size).stream()).collect(Collectors.toList());
        List<G> newSecond = Stream.concat(
                other.genesCopy().subList(0, point).stream(),
                genesCopy().subList(point, size).stream()).collect(Collectors.toList());
        genes = newFirst;
        other.genes = newSecond;
    }

}
