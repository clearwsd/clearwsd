package io.github.clearwsd.feature.resource;

import com.google.common.collect.Multimap;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import lombok.experimental.Accessors;

/**
 * TSV resource initializer used to initialize a {@link MultimapResource} from an {@link InputStream}.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class DefaultTsvResourceInitializer<K> extends TsvResourceInitializer<K> {

    private static final long serialVersionUID = -1044802169525334439L;

    public DefaultTsvResourceInitializer(String key, URL path) {
        super(key, path);
    }

    public DefaultTsvResourceInitializer(String key, String path) {
        super(key, path);
    }

    @Override
    protected void apply(List<String> fields, Multimap<String, String> multimap) {
        String key = keyFunction.apply(fields.get(0));
        fields.subList(1, fields.size()).stream()
                .map(s -> valueFunction.apply(s))
                .forEach(s -> multimap.put(key, s));
    }

}
