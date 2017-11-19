package edu.colorodo.clear.wsd.feature.extractor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

import static edu.colorodo.clear.wsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Feature extractor that looks up the values corresponding to an associated key or keys.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class StringListLookupFeature<T extends NlpInstance> extends NlpFeatureExtractor<T, List<String>> {

    private static final long serialVersionUID = -5294861373939396232L;

    @JsonProperty
    private List<String> keys;

    public StringListLookupFeature(@JsonProperty("keys") List<String> keys) {
        this.keys = keys;
        id = String.join(KEY_DELIM, keys);
    }

    public StringListLookupFeature(String key) {
        this(Collections.singletonList(key));
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
