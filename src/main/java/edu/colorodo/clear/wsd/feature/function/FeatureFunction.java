package edu.colorodo.clear.wsd.feature.function;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.type.NlpInstance;

/**
 * Feature function that produces a list of features given a context and an extractor.
 *
 * @param <InputT> input type
 * @author jamesgung
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@c")
public interface FeatureFunction<InputT extends NlpInstance> {

    /**
     * Given an NLP instance type, produce a corresponding list of categorical features.
     *
     * @param input input NLP instance
     * @return list of string features
     */
    List<StringFeature> apply(InputT input);

}
