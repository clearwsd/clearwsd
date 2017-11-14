package edu.colorodo.clear.wsd.type;

import java.util.Map;

/**
 * NLP instance, such as for a token or sentence, used during feature extraction and as a common interface for outputs of different
 * NLP pre-processing systems.
 *
 * @author jamesgung
 */
public interface NlpInstance {

    /**
     * Identifier/index in container of this instance.
     */
    int index();

    /**
     * Map of features.
     */
    Map<String, String> features();

    /**
     * Return the feature for a corresponding feature type.
     *
     * @param featureType feature type
     * @return feature value
     */
    String feature(FeatureType featureType);

    /**
     * Return the feature for a corresponding feature key.
     *
     * @param feature feature key
     * @return feature value
     */
    String feature(String feature);

    /**
     * Add a feature to this instance of a given type.
     *
     * @param featureType feature type
     * @param value       feature value
     */
    void addFeature(FeatureType featureType, String value);

    /**
     * Add a feature with a given key to this instance.
     *
     * @param featureKey feature key
     * @param value      feature value
     */
    void addFeature(String featureKey, String value);

}
