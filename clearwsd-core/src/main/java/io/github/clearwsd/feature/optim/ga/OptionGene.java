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
