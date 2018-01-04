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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.extractor.string.StringFunction;

import static io.github.clearwsd.feature.util.FeatureUtils.KEY_DELIM;

/**
 * Extractor that performs a series of string functions to the output of a base extractor.
 *
 * @author jamesgung
 */
public class StringFunctionExtractor<T extends NlpInstance> implements StringExtractor<T> {

    private static final long serialVersionUID = -6703070360030390063L;

    private FeatureExtractor<T, String> baseExtractor;
    private List<StringFunction> stringFunctions;
    private String id;

    public StringFunctionExtractor(FeatureExtractor<T, String> baseExtractor, List<StringFunction> stringFunctions) {
        this.baseExtractor = baseExtractor;
        this.stringFunctions = stringFunctions;
        id = baseExtractor.id() + KEY_DELIM + this.stringFunctions.stream()
                .map(StringFunction::id)
                .collect(Collectors.joining(KEY_DELIM));
    }

    public StringFunctionExtractor(FeatureExtractor<T, String> baseExtractor, StringFunction stringFunction) {
        this(baseExtractor, Collections.singletonList(stringFunction));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String extract(T instance) {
        String result = baseExtractor.extract(instance);
        for (StringFunction function : stringFunctions) {
            result = function.apply(result);
        }
        return result;
    }

}
