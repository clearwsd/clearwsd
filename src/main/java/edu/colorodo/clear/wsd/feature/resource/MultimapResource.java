package edu.colorodo.clear.wsd.feature.resource;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableListMultimap;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import edu.colorodo.clear.wsd.feature.util.TsvResourceInitializer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Multimap-based resource. Can define an arbitrary initializer for loading the multimap. Function applied to keys upon lookup, and
 * function applied to values in multimap are also configurable.
 *
 * @author jamesgung
 */
@Slf4j
@Setter
@Getter
@Accessors(fluent = true)
public class MultimapResource<K> implements FeatureResource<K, List<String>> {

    private String key;
    private ImmutableListMultimap<String, String> multimap;
    private Function<String, String> valueFunction = value -> value;
    private Function<String, String> keyFunction = key -> key;
    private Function<K, String> mappingFunction = Object::toString;
    private BiConsumer<MultimapResource<K>, InputStream> initializer = new TsvResourceInitializer<>();

    public MultimapResource(String key) {
        this.key = key;
    }

    @Override
    public List<String> lookup(K key) {
        return multimap.get(mappingFunction.apply(key));
    }

    @Override
    public void initialize(InputStream inputStream) {
        Stopwatch sw = Stopwatch.createStarted();
        initializer.accept(this, inputStream);
        log.debug("Resource {} initialized {} entries in {}.", key, multimap.keys().size(), sw);
    }

}
