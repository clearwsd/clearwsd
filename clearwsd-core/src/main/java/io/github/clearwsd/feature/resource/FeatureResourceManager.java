/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.feature.resource;

import java.util.function.Supplier;

/**
 * Feature resource manager.
 *
 * @author jamesgung
 */
public interface FeatureResourceManager {

    /**
     * Force initialization of any lazily-loaded resources/proxies.
     */
    void initialize();

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