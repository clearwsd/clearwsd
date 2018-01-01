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

package io.github.clearwsd.feature.annotator;

import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.resource.FeatureResource;
import io.github.clearwsd.feature.resource.FeatureResourceManager;

/**
 * Base resource annotator.
 *
 * @author jamesgung
 */
public abstract class ResourceAnnotator<T, S extends NlpInstance> implements Annotator<S> {

    private static final long serialVersionUID = -4329097009920614601L;

    protected String resourceKey;
    protected FeatureResource<T, List<String>> resource;

    public ResourceAnnotator(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public boolean initialized() {
        return resource != null;
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        this.resource = featureResourceManager.getResource(resourceKey);
    }

}
