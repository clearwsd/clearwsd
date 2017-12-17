package edu.colorado.clear.wsd.feature.function;

import java.io.Serializable;
import java.util.List;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.StringFeature;

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