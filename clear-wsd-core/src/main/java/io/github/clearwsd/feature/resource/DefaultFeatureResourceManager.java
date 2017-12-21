package io.github.clearwsd.feature.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Default resource manager implementation.
 *
 * @author jamesgung
 */
public class DefaultFeatureResourceManager implements FeatureResourceManager, Serializable {

    private static final long serialVersionUID = -8748192893966732273L;

    private Map<String, FeatureResourceProxy> resourceMap = new HashMap<>();

    @Override
    public void initialize() {
        for (Map.Entry<String, FeatureResourceProxy> entry : resourceMap.entrySet()) {
            FeatureResourceProxy resource = entry.getValue();
            resource.initialize();
        }
    }

    @Override
    public <ResourceT extends FeatureResource> ResourceT getResource(String identifier) {
        try {
            //noinspection unchecked
            return (ResourceT) resourceMap.get(identifier);
        } catch (ClassCastException e) {
            throw new RuntimeException(String.format(
                    "Found resource with identifier \"%s\", but with the incorrect type: %s", identifier, e.getMessage()), e);
        }
    }

    @Override
    public <ResourceT extends FeatureResource> FeatureResourceManager registerInitializer(
            String identifier, Supplier<ResourceT> initializer) {
        //noinspection unchecked
        resourceMap.put(identifier, new FeatureResourceProxy(identifier, initializer));
        return this;
    }

}