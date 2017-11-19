package edu.colorodo.clear.wsd.feature.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Default resource manager implementation.
 *
 * @author jamesgung
 */
public class DefaultFeatureResourceManager implements FeatureResourceManager {

    private Map<String, FeatureResource> resourceMap = new HashMap<>();

    public DefaultFeatureResourceManager addResource(String key, FeatureResource resource) {
        resourceMap.put(key, resource);
        return this;
    }

    @Override
    public <K, T> FeatureResource<K, T> getResource(String key) {
        //noinspection unchecked
        return resourceMap.get(key);
    }

}