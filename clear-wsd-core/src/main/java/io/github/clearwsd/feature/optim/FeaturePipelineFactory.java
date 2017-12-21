package io.github.clearwsd.feature.optim;

import io.github.clearwsd.feature.pipeline.FeaturePipeline;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.pipeline.FeaturePipeline;

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
