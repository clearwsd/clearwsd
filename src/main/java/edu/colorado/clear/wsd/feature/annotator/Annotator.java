package edu.colorado.clear.wsd.feature.annotator;

import java.io.Serializable;

import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorado.clear.wsd.type.NlpInstance;

/**
 * NLP annotator, used to apply new features to a given input (typically as a pre-processing step).
 *
 * @author jamesgung
 */
public interface Annotator<T extends NlpInstance> extends Serializable {

    T annotate(T instance);

    /**
     * Dependency injection for feature resources.
     *
     * @param featureResourceManager resource manager
     */
    default void initialize(FeatureResourceManager featureResourceManager) {
        // pass by default
    }

}
