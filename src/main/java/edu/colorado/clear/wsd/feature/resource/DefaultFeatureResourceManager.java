package edu.colorado.clear.wsd.feature.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Default resource manager implementation.
 *
 * @author jamesgung
 */
public class DefaultFeatureResourceManager implements FeatureResourceManager {

    private Map<String, FeatureResource> resourceMap = new HashMap<>();
    private transient Map<String, Supplier<FeatureResource>> initializerMap = new HashMap<>();

    public <ResourceT extends FeatureResource> DefaultFeatureResourceManager addResource(String key, ResourceT resource) {
        resourceMap.put(key, resource);
        return this;
    }

    /**
     * Initialize all resources.
     */
    public void initialize() {
        for (Map.Entry<String, Supplier<FeatureResource>> entry : initializerMap.entrySet()) {
            Supplier<FeatureResource> resource = entry.getValue();
            if (!resourceMap.containsKey(entry.getKey())) {
                resourceMap.put(entry.getKey(), resource.get());
            }
        }
    }

    private <I extends FeatureResource> Supplier<I> getInitializer(String identifier) {
        Supplier<I> initializer;
        try {
            //noinspection unchecked
            initializer = (Supplier<I>) initializerMap.get(identifier);
        } catch (ClassCastException e) {
            throw new RuntimeException(String.format(
                    "Found initializer for identifier \"%s\", but with the incorrect type: %s", identifier, e.getMessage()), e);
        }
        if (initializer == null) {
            throw new IllegalArgumentException("Missing initializer for provided identifier: " + identifier);
        }
        return initializer;
    }

    @Override
    public <ResourceT extends FeatureResource> ResourceT getResource(String identifier) {
        try {
            //noinspection unchecked
            ResourceT result = (ResourceT) resourceMap.get(identifier);
            if (result == null) {
                Supplier<ResourceT> initializer = getInitializer(identifier);
                result = initializer.get();
            }
            return result;
        } catch (ClassCastException e) {
            throw new RuntimeException(String.format(
                    "Found resource with identifier \"%s\", but with the incorrect type: %s", identifier, e.getMessage()), e);
        }
    }

    @Override
    public <ResourceT extends FeatureResource> FeatureResourceManager registerInitializer(
            String identifier, Supplier<ResourceT> initializer) {
        //noinspection unchecked
        initializerMap.put(identifier, (Supplier<FeatureResource>) initializer);
        return this;
    }

}