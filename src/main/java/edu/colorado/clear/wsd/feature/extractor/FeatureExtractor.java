package edu.colorado.clear.wsd.feature.extractor;

import java.io.Serializable;

/**
 * Feature extractor interface.
 *
 * @param <T> input type for feature extraction
 * @param <S> feature output type
 * @author jamesgung
 */
public interface FeatureExtractor<T, S> extends Serializable {

    /**
     * Id used to automatically create human-readable (non-unique) identifiers for each resulting extracted features.
     */
    String id();

    /**
     * Extract a feature corresponding to a given instance.
     *
     * @param instance NLP instance
     * @return feature output
     */
    S extract(T instance);

}
