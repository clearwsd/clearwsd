package edu.colorodo.clear.wsd.feature.extractor;

import java.util.Collections;
import java.util.List;

import edu.colorodo.clear.wsd.type.NlpInstance;

import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Feature extractor that looks up an associated key or keys, optionally falling back to another extractor if nothing is found.
 *
 * @author jamesgung
 */
public class LookupFeatureExtractor<T extends NlpInstance> extends NlpFeatureExtractor<T, String> {

    public static final String DEFAULT_VALUE = "<NONE>";

    private List<String> keys;
    private FeatureExtractor<T, String> fallbackExtractor;

    public LookupFeatureExtractor(List<String> keys, FeatureExtractor<T, String> fallbackExtractor) {
        this.keys = keys;
        this.fallbackExtractor = fallbackExtractor;
        id = String.join(KEY_DELIM, keys);
    }

    public LookupFeatureExtractor(List<String> keys) {
        this(keys, null);
    }

    public LookupFeatureExtractor(String key) {
        this(Collections.singletonList(key));
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
