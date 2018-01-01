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

package io.github.clearwsd.feature.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

import io.github.clearwsd.feature.model.BaseVocabulary;
import io.github.clearwsd.feature.model.Vocabulary;
import io.github.clearwsd.feature.model.BaseVocabulary;
import io.github.clearwsd.feature.model.Vocabulary;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Vocabulary builder used during feature extraction to keep track of feature counts, and produce vector representations of inputs.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class VocabularyBuilder implements Vocabulary {

    private static final long serialVersionUID = 4581175527928695153L;

    @Getter
    private Map<String, Integer> counts = new HashMap<>();
    @Getter
    private BiMap<String, Integer> indices = HashBiMap.create();

    public int index(String feature) {
        counts.merge(feature, 1, (old, val) -> old + val);
        return indices.computeIfAbsent(feature, none -> indices.size());
    }

    @Override
    public String value(int index) {
        return indices.inverse().get(index);
    }

    public Vocabulary build() {
        return new BaseVocabulary(indices);
    }

}
