package io.github.clearwsd.feature.annotator;

import java.io.Serializable;

import io.github.clearwsd.feature.resource.FeatureResourceManager;

/**
 * NLP annotator, used to apply new features to a given input (typically as a pre-processing step).
 *
 * @author jamesgung
 */
public interface Annotator<T> extends Serializable {

    T annotate(T instance);

    boolean initialized();

    /**
     * Dependency injection for feature resources.
     *
     * @param featureResourceManager resource manager
     */
    default void initialize(FeatureResourceManager featureResourceManager) {
        // pass by default
    }

}
