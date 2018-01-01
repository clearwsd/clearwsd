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

package io.github.clearwsd.type;

import java.util.Map;

/**
 * NLP instance, such as for a token or sentence, used during feature extraction and as a common interface for outputs of different
 * NLP pre-processing systems.
 *
 * @author jamesgung
 */
public interface NlpInstance {

    /**
     * Identifier/index in container of this instance.
     */
    int index();

    /**
     * Map of features associated with this instance.
     */
    Map<String, Object> features();

    /**
     * Return the feature for a corresponding feature type.
     *
     * @param featureType feature type
     * @param <T>         type of resulting feature
     * @return feature value
     */
    <T> T feature(FeatureType featureType);

    /**
     * Return the feature for a corresponding feature key.
     *
     * @param feature feature key
     * @param <T>     type of resulting feature
     * @return feature value
     */
    <T> T feature(String feature);

    /**
     * Add a feature to this instance of a given type.
     *
     * @param featureType feature type
     * @param value       feature value
     * @param <T>         feature value type
     */
    <T> void addFeature(FeatureType featureType, T value);

    /**
     * Add a feature with a given key to this instance.
     *
     * @param featureKey feature key
     * @param value      feature value
     * @param <T>        value type
     */
    <T> void addFeature(String featureKey, T value);

}
