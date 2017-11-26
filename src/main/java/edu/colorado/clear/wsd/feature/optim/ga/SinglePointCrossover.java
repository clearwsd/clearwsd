package edu.colorado.clear.wsd.feature.optim.ga;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Single point crossover op.
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class SinglePointCrossover<G extends Gene> implements CrossoverOp<G> {

    @Setter
    private Random random = new Random();

    @Override
    public List<Chromosome<G>> apply(Chromosome<G> first, Chromosome<G> second) {
        Chromosome<G> firstCopy = first.copy();
        Chromosome<G> secondCopy = second.copy();
        List<G> firstGenes = firstCopy.genes();
        List<G> secondGenes = secondCopy.genes();
        Preconditions.checkArgument(firstGenes.size() == secondGenes.size(),
                "Number of genes must be equal (was %d vs. %d).", firstGenes.size(), secondGenes.size());
        int size = firstGenes.size();
        int point = random.nextInt(size);
        List<G> newFirst = Stream.concat(
                firstGenes.subList(0, point).stream(),
                secondGenes.subList(point, size).stream()).collect(Collectors.toList());
        List<G> newSecond = Stream.concat(
                secondGenes.subList(0, point).stream(),
                firstGenes.subList(point, size).stream()).collect(Collectors.toList());
        firstCopy.genes(newFirst);
        secondCopy.genes(newSecond);
        return Arrays.asList(firstCopy, secondCopy);
    }

}
