package edu.colorado.clear.type;

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
     * Map of features associated with this instance.
     */
    Map<String, Object> features();

    /**
     * Return the feature for a corresponding feature type.
     *
     * @param featureType feature type
     * @param <T>         type of resulting feature
     * @return feature value
     */
    <T> T feature(FeatureType featureType);

    /**
     * Return the feature for a corresponding feature key.
     *
     * @param feature feature key
     * @param <T>     type of resulting feature
     * @return feature value
     */
    <T> T feature(String feature);

    /**
     * Add a feature to this instance of a given type.
     *
     * @param featureType feature type
     * @param value       feature value
     * @param <T>         feature value type
     */
    <T> void addFeature(FeatureType featureType, T value);

    /**
     * Add a feature with a given key to this instance.
     *
     * @param featureKey feature key
     * @param value      feature value
     * @param <T>        value type
     */
    <T> void addFeature(String featureKey, T value);

}
