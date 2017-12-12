package edu.colorado.clear.wsd.type;

import java.util.Comparator;
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
    private Map<String, Object> features;

    public BaseNlpInstance(int index) {
        this.index = index;
        this.features = new HashMap<>();
    }

    @Override
    public <T> T feature(FeatureType featureType) {
        //noinspection unchecked
        return (T) features.get(featureType.name());
    }

    @Override
    public <T> T feature(String feature) {
        //noinspection unchecked
        return (T) features.get(feature);
    }

    @Override
    public <T> void addFeature(FeatureType featureType, T value) {
        features.put(featureType.name(), value);
    }

    @Override
    public <T> void addFeature(String featureKey, T value) {
        features.put(featureKey, value);
    }

    @Override
    public String toString() {
        return index + "\t" + features.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map((e) -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining("\t"));
    }
}
