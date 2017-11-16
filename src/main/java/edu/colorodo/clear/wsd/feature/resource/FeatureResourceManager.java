package edu.colorodo.clear.wsd.feature.resource;

/**
 * Feature resource manager.
 *
 * @param <K> key type
 * @author jamesgung
 */
public interface FeatureResourceManager<K> {

    /**
     * Get the feature resource associated with a particular key.
     *
     * @param key input key
     * @param <T> resource value type
     * @return feature resource
     */
    <T> FeatureResource<T, K> getResource(K key);

}
