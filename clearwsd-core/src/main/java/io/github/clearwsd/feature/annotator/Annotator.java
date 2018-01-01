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

import java.io.Serializable;

import io.github.clearwsd.feature.resource.FeatureResourceManager;

/**
 * NLP annotator, used to apply new features to a given input (typically as a pre-processing step).
 *
 * @author jamesgung
 */
public interface Annotator<T> extends Serializable {

    T annotate(T instance);

    boolean initialized();

    /**
     * Dependency injection for feature resources.
     *
     * @param featureResourceManager resource manager
     */
    default void initialize(FeatureResourceManager featureResourceManager) {
        // pass by default
    }

}
