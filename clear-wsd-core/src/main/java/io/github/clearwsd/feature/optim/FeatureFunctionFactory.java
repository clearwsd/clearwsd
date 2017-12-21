package io.github.clearwsd.feature.optim;

import io.github.clearwsd.feature.function.FeatureFunction;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.function.FeatureFunction;

/**
 * Feature function generator.
 *
 * @author jamesgung
 */
public interface FeatureFunctionFactory<T extends NlpInstance> {

    /**
     * Instantiate a feature function.
     */
    FeatureFunction<T> create();

}
