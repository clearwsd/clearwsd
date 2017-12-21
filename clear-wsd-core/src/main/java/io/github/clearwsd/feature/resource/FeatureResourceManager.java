package io.github.clearwsd.feature.resource;

import java.util.function.Supplier;

/**
 * Feature resource manager.
 *
 * @author jamesgung
 */
public interface FeatureResourceManager {

    /**
     * Retrieve the feature resource with the provided identifier, initializing the resource if it is not available.
     *
     * @param identifier  feature resource identifier
     * @param <ResourceT> type of resource
     * @return requested resource
     */
    <ResourceT extends FeatureResource> ResourceT getResource(String identifier);

    /**
     * Register an initializer for a resource to the manager.
     *
     * @param identifier  feature resource identifier
     * @param initializer feature resource initializer
     * @param <ResourceT> type of resource
     */
    <ResourceT extends FeatureResource> FeatureResourceManager registerInitializer(String identifier,
                                                                                   Supplier<ResourceT> initializer);

}