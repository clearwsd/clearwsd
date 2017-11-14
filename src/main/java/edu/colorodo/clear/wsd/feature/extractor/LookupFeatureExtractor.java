package edu.colorodo.clear.wsd.feature.extractor;

import java.util.List;

import edu.colorodo.clear.wsd.type.NlpInstance;

import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Feature extractor that looks up an associated key or keys, optionally falling back to another extractor if nothing is found.
 *
 * @author jamesgung
 */
public class LookupFeatureExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T> {

    public static final String DEFAULT_VALUE = "<NONE>";

    private List<String> keys;
    private FeatureExtractor<T> fallbackExtractor;

    public LookupFeatureExtractor(List<String> keys) {
        this(keys, null);
    }

    public LookupFeatureExtractor(List<String> keys, FeatureExtractor<T> fallbackExtractor) {
        this.keys = keys;
        this.fallbackExtractor = fallbackExtractor;
        id = String.join(KEY_DELIM, keys);
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
