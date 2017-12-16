package edu.colorado.clear.wsd.feature.optim;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;

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
