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

import java.util.Map;

import io.github.clearwsd.type.NlpInstance;

/**
 * Extractor that maps the values output by a base extractor into pre-defined categories.
 *
 * @author jamesgung
 */
public class MappingExtractor<T extends NlpInstance> implements StringExtractor<T> {

    private static final long serialVersionUID = -1364779820794809734L;

    private FeatureExtractor<T, String> baseExtractor;
    private Map<String, String> stringMap;

    public MappingExtractor(FeatureExtractor<T, String> baseExtractor, Map<String, String> stringMap) {
        this.baseExtractor = baseExtractor;
        this.stringMap = stringMap;
    }

    @Override
    public String id() {
        return baseExtractor.id();
    }

    @Override
    public String extract(T instance) {
        String input = baseExtractor.extract(instance);
        return stringMap.getOrDefault(input, input);
    }

}
