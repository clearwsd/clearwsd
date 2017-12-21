package io.github.clearwsd.feature.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.util.FeatureUtils;

/**
 * Feature extractor that looks up the values corresponding to an associated key or keys.
 *
 * @author jamesgung
 */
public class ListLookupFeatureExtractor<T extends NlpInstance> implements StringListExtractor<T> {

    private static final long serialVersionUID = -5294861373939396232L;

    private List<String> keys;
    private String id;

    public ListLookupFeatureExtractor(List<String> keys) {
        this.keys = keys;
        id = String.join(FeatureUtils.KEY_DELIM, keys);
    }

    public ListLookupFeatureExtractor(String key) {
        this(Collections.singletonList(key));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<String> extract(T instance) {
        List<String> results = new ArrayList<>();
        for (String key : keys) {
            List<String> feature = instance.feature(key);
            if (feature != null) {
                results.addAll(feature);
            }
        }
        return results;
    }

}
