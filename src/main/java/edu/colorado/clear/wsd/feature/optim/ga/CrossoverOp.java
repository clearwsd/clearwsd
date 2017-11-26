package edu.colorado.clear.wsd.feature.optim.ga;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Crossover operation.
 *
 * @author jamesgung
 */
public interface CrossoverOp<G extends Gene> extends BiFunction<Chromosome<G>, Chromosome<G>, List<Chromosome<G>>> {

}
