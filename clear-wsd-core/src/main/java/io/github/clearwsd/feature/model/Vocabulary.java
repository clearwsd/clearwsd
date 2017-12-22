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

package io.github.clearwsd.feature.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Feature vocabulary used to convert features to one-hot representations.
 *
 * @author jamesgung
 */
public interface Vocabulary extends Serializable {

    /**
     * Return a map of features to corresponding indices.
     */
    Map<String, Integer> indices();

    /**
     * Return an index for a given feature, or a default value if it is not found.
     *
     * @param value input feature
     * @return corresponding index
     */
    int index(String value);

    /**
     * Return a feature for a given index, or null if none is found.
     *
     * @param index input index
     * @return feature
     */
    String value(int index);

}
