package edu.colorodo.clear.wsd.type;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * NLP instance default implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class BaseNlpInstance implements NlpInstance {

    private int index;
    private Map<String, String> features;

    public BaseNlpInstance(int index) {
        this.index = index;
        this.features = new HashMap<>();
    }

    @Override
    public String feature(FeatureType featureType) {
        return features.get(featureType.name());
    }

    @Override
    public String feature(String feature) {
        return features.get(feature);
    }

    @Override
    public void addFeature(FeatureType featureType, String value) {
        features.put(featureType.name(), value);
    }

    @Override
    public void addFeature(String featureKey, String value) {
        features.put(featureKey, value);
    }

    @Override
    public String toString() {
        return index + "\t" + features.entrySet().stream()
                .map((e) -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining("\t"));
    }
}
