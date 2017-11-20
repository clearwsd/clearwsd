package edu.colorado.clear.wsd.feature.context;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

import edu.colorado.clear.wsd.type.NlpInstance;

/**
 * Context factory used to extract instances for feature extraction for arbitrary NLP tasks.
 *
 * @param <InputT>  input type
 * @param <OutputT> output type used as input to a feature extractor
 * @author jamesgung
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@c")
public interface NlpContextFactory<InputT extends NlpInstance, OutputT extends NlpInstance> {

    /**
     * Create a context given an input instance.
     *
     * @param instance input instance
     * @return context
     */
    List<NlpContext<OutputT>> apply(InputT instance);

}
