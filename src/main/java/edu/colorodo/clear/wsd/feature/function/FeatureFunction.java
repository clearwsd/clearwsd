package edu.colorodo.clear.wsd.feature.function;

import java.util.List;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.type.NlpInstance;

/**
 * Feature function that produces a list of features given a context and an extractor.
 *
 * @param <InputT> input type
 * @author jamesgung
 */
public interface FeatureFunction<InputT extends NlpInstance> {

    /**
     * Given an NLP instance type, produce a corresponding list of categorical features.
     *
     * @param input input NLP instance
     * @return list of string features
     */
    List<StringFeature> apply(InputT input);

}
