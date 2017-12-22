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

package io.github.clearwsd.feature.extractor;

import java.io.Serializable;

/**
 * Feature extractor interface.
 *
 * @param <T> input type for feature extraction
 * @param <S> feature output type
 * @author jamesgung
 */
public interface FeatureExtractor<T, S> extends Serializable {

    /**
     * Id used to automatically create human-readable (non-unique) identifiers for each resulting extracted features.
     */
    String id();

    /**
     * Extract a feature corresponding to a given instance.
     *
     * @param instance NLP instance
     * @return feature output
     */
    S extract(T instance);

}
