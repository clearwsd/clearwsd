package io.github.clearwsd.type;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default {@link NlpInstance} implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DefaultNlpInstance implements NlpInstance {

    @Setter
    private int index;
    private Map<String, Object> features;

    public DefaultNlpInstance(int index) {
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
