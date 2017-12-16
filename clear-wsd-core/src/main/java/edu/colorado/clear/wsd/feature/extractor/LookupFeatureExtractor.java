package edu.colorado.clear.wsd.feature.extractor;

import java.util.Collections;
import java.util.List;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.util.FeatureUtils;

/**
 * Feature extractor that looks up an associated key or keys, optionally falling back to another extractor if nothing is found.
 *
 * @author jamesgung
 */
public class LookupFeatureExtractor<T extends NlpInstance> implements StringExtractor<T> {

    private static final String DEFAULT_VALUE = "<NONE>";
    private static final long serialVersionUID = -8167041300890840929L;

    private List<String> keys;
    private FeatureExtractor<T, String> fallbackExtractor;
    private String id;

    public LookupFeatureExtractor(List<String> keys, FeatureExtractor<T, String> fallbackExtractor) {
        this.keys = keys;
        this.fallbackExtractor = fallbackExtractor;
        this.id = String.join(FeatureUtils.KEY_DELIM, keys);
    }

    public LookupFeatureExtractor(List<String> keys) {
        this(keys, null);
    }

    public LookupFeatureExtractor(String key) {
        this(Collections.singletonList(key));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String extract(T instance) {
        for (String key : keys) {
            String feature = instance.feature(key);
            if (feature != null) {
                return feature;
            }
        }
        if (fallbackExtractor == null) {
            return DEFAULT_VALUE;
        }
        return fallbackExtractor.extract(instance);
    }

}
