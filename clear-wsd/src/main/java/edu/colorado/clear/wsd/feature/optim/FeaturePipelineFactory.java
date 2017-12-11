package edu.colorado.clear.wsd.feature.optim;

import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.type.NlpInstance;

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
