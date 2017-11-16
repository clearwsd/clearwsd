package edu.colorodo.clear.wsd.feature.extractor;

import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Base feature extractor.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public abstract class NlpFeatureExtractor<T extends NlpInstance, S> implements FeatureExtractor<T, S> {

    protected String id;

    public NlpFeatureExtractor() {
        this.id = this.getClass().getSimpleName();
    }
}
