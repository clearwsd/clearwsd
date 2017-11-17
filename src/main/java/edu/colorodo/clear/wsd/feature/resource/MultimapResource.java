package edu.colorodo.clear.wsd.feature.resource;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableListMultimap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.InputStream;
import java.util.List;

import edu.colorodo.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorodo.clear.wsd.feature.extractor.IdentityFeatureExtractor;
import edu.colorodo.clear.wsd.feature.extractor.string.IdentityStringFunction;
import edu.colorodo.clear.wsd.feature.extractor.string.StringFunction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Multimap-based resource. Can define an arbitrary initializer for loading the multimap. Function applied to keys upon lookup, and
 * function applied to values in multimap are also configurable.
 *
 * @param <K> input key type used during lookup
 * @author jamesgung
 */
@Slf4j
@Setter
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MultimapResource<K> implements FeatureResource<K, List<String>> {

    private String key;
    @JsonIgnore
    private ImmutableListMultimap<String, String> multimap;
    private StringFunction valueFunction = new IdentityStringFunction();
    private StringFunction keyFunction = new IdentityStringFunction();
    private FeatureExtractor<K, String> mappingFunction = new IdentityFeatureExtractor<>();
    private ResourceInitializer<MultimapResource<K>> initializer = new TsvResourceInitializer<>();

    public MultimapResource(String key) {
        this.key = key;
    }

    @Override
    public List<String> lookup(K key) {
        return multimap.get(mappingFunction.extract(key));
    }

    @Override
    public void initialize(InputStream inputStream) {
        Stopwatch sw = Stopwatch.createStarted();
        initializer.accept(this, inputStream);
        log.debug("Resource {} initialized {} entries in {}.", key, multimap.keys().size(), sw);
    }

}
