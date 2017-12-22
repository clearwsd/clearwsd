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

import com.google.common.collect.ImmutableListMultimap;

import java.util.List;

import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.IdentityFeatureExtractor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Multimap-based resource. Can define an arbitrary initializer for loading the multimap.
 * Function applied to keys upon lookup, and function applied to values in multimap are also configurable.
 *
 * @param <K> input key type used during lookup
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class MultimapResource<K> implements FeatureResource<K, List<String>> {

    private String key;
    private ImmutableListMultimap<String, String> multimap;
    private FeatureExtractor<K, String> mappingFunction = new IdentityFeatureExtractor<>();

    public MultimapResource(String key) {
        this.key = key;
    }

    @Override
    public List<String> lookup(K key) {
        return multimap.get(mappingFunction.extract(key));
    }

}
