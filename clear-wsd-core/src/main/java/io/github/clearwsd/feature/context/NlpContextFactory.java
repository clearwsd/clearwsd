package io.github.clearwsd.feature.context;

import java.io.Serializable;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;

/**
 * Context factory used to extract instances for feature extraction for arbitrary NLP tasks.
 *
 * @param <InputT>  input type
 * @param <OutputT> output type used as input to a feature extractor
 * @author jamesgung
 */
public interface NlpContextFactory<InputT, OutputT extends NlpInstance> extends Serializable {

    /**
     * Create a context given an input instance.
     *
     * @param instance input instance
     * @return context
     */
    List<NlpContext<OutputT>> apply(InputT instance);

}
