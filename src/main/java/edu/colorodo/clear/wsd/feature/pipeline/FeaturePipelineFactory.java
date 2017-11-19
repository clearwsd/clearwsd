package edu.colorodo.clear.wsd.feature.pipeline;

import java.io.InputStream;

import edu.colorodo.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorodo.clear.wsd.type.NlpInstance;
import edu.colorodo.clear.wsd.utils.SerializationUtils;

/**
 * Feature pipeline factory.
 *
 * @author jamesgung
 */
public class FeaturePipelineFactory<I extends NlpInstance> {

    public FeatureResourceManager resourceManager;

    public FeaturePipeline<I> create(InputStream inputStream) {
        return SerializationUtils.readFromJson(inputStream, DefaultFeaturePipeline.class);
    }

}
