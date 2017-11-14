package edu.colorodo.clear.wsd.feature.extractor;

/**
 * Feature extractor interface.
 *
 * @param <T> input type for feature extraction
 * @author jamesgung
 */
public interface FeatureExtractor<T> {

    /**
     * Id used to automatically create human-readable (non-unique) identifiers for each resulting extracted features.
     */
    String id();

    /**
     * Extract a string feature corresponding to a given instance.
     *
     * @param instance NLP instance
     * @return string feature
     */
    String extract(T instance);

}
