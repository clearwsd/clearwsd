package edu.colorodo.clear.wsd.feature.util;

/**
 * Feature extraction constants and utilities.
 *
 * @author jamesgung
 */
public class FeatureUtils {

    // separator for parts of feature extractor IDs
    public static final String KEY_DELIM = ".";
    // separator for concatenated feature values
    public static final String CONCAT_DELIM = "|";
    // separator for combing feature/context keys
    public static final String CONTEXT_FEATURE_SEP = "::";
    // separator for concatenated features from multiple contexts
    public static final String CONTEXT_DELIM = "__";
    // separator between feature ID and feature value
    public static final String FEATURE_ID_SEP = ":";

    public static String computeId(String contextId, String featureId) {
        return contextId + CONTEXT_FEATURE_SEP + featureId;
    }

}
