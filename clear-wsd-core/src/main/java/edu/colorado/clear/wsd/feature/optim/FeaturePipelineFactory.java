package edu.colorado.clear.wsd.feature.optim;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;

/**
 * Feature pipeline generator.
 *
 * @author jamesgung
 */
public interface FeaturePipelineFactory<T extends NlpInstance> {

    /**
     * Instantiate a feature pipeline.
     */
    FeaturePipeline<T> create();

}
