package edu.colorado.clear.wsd.feature.optim.ga;

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
@Accessors(fluent = true)
@AllArgsConstructor
public class DefaultChromosome<G extends Gene> implements Chromosome<G> {

    @Getter
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
        DefaultChromosome<G> copy = new DefaultChromosome<>(genesCopy(), random);
        copy.random = random;
        return copy;
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
