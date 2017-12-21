package io.github.clearwsd.feature.optim.ga;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Gene with a fixed number of possible values (including inactive), selected from uniformly during mutation.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class OptionGene<T> implements Gene {

    private T currentValue;
    private boolean active;
    private Random random;

    private double activationProbability;
    private ImmutableList<T> possibleValues;

    public OptionGene(List<T> possibleValues, Random random, double activationProbability) {
        this.random = random;
        this.activationProbability = activationProbability;
        this.possibleValues = ImmutableList.copyOf(possibleValues);
    }

    @Override
    public void mutate() {
        active = random.nextDouble() < activationProbability;
        if (active) {
            this.currentValue = possibleValues.get(random.nextInt(possibleValues.size()));
        }
    }

    @Override
    public OptionGene<T> copy() {
        OptionGene<T> gene = new OptionGene<>(this.possibleValues, random, activationProbability);
        gene.currentValue = this.currentValue;
        gene.active = this.active;
        return gene;
    }
}
