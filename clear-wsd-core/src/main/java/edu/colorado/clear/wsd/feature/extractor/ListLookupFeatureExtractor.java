package edu.colorado.clear.wsd.feature.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorado.clear.wsd.feature.util.FeatureUtils;
import edu.colorado.clear.wsd.type.NlpInstance;

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
