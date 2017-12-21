package io.github.clearwsd.feature.resource;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Feature resource proxy.
 *
 * @author jamesgung
 */
public class FeatureResourceProxy<K, T> implements FeatureResource<K, T>, Serializable {

    private static final long serialVersionUID = 2637440121117573645L;

    private String key;
    private Supplier<FeatureResource<K, T>> initializer;
    private transient FeatureResource<K, T> resource;

    public FeatureResourceProxy(String key, Supplier<FeatureResource<K, T>> initializer) {
        this.key = key;
        this.initializer = initializer;
    }

    public void initialize() {
        if (resource == null) {
            resource = initializer.get();
        }
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public T lookup(K key) {
        initialize();
        return resource.lookup(key);
    }

}
