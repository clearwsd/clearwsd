package edu.colorodo.clear.wsd.feature.extractor;

import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Base feature extractor.
 *
 * @author jamesgung
 */
public abstract class NlpFeatureExtractor<T extends NlpInstance, S> implements FeatureExtractor<T, S> {

    @Getter
    @Accessors(fluent = true)
    protected String id;

    public NlpFeatureExtractor() {
        this.id = this.getClass().getSimpleName();
    }
}
