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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default vocabulary implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class BaseVocabulary implements Vocabulary {

    private static final long serialVersionUID = 5268053360096219564L;

    private BiMap<String, Integer> indices;

    @Setter
    private int defaultIndex = 0;

    public BaseVocabulary(Map<String, Integer> indices) {
        this.indices = HashBiMap.create(indices);
    }

    @Override
    public int index(String value) {
        return indices.getOrDefault(value, 0);
    }

    @Override
    public String value(int index) {
        return indices.inverse().get(index);
    }

}
