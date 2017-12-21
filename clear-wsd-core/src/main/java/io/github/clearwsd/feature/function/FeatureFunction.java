package io.github.clearwsd.feature.function;

import java.io.Serializable;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;

/**
 * Feature function that produces a list of features given a context and an extractor.
 *
 * @param <InputT> input type
 * @author jamesgung
 */
public interface FeatureFunction<InputT extends NlpInstance> extends Serializable {

    /**
     * Given an NLP instance type, produce a corresponding list of categorical features.
     *
     * @param input input NLP instance
     * @return list of string features
     */
    List<StringFeature> apply(InputT input);

}
