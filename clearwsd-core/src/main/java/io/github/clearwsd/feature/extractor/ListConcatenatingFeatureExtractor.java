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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.util.FeatureUtils;

/**
 * Feature extractor concatenating the results of multiple different extractors, the base extractor extracting multiple strings as
 * features.
 *
 * @author jamesgung
 */
public class ListConcatenatingFeatureExtractor<T extends NlpInstance> implements StringListExtractor<T> {

    private static final long serialVersionUID = -8997941881477825340L;

    private FeatureExtractor<T, List<String>> baseExtractor;
    private List<FeatureExtractor<T, String>> extractors;
    private String id;

    public ListConcatenatingFeatureExtractor(FeatureExtractor<T, List<String>> baseExtractor,
                                             List<FeatureExtractor<T, String>> extractors) {
        this.baseExtractor = baseExtractor;
        this.extractors = extractors;
        id = baseExtractor.id() + FeatureUtils.KEY_DELIM
                + extractors.stream().map(FeatureExtractor::id).collect(Collectors.joining(FeatureUtils.KEY_DELIM));
    }

    @SafeVarargs
    public ListConcatenatingFeatureExtractor(FeatureExtractor<T, List<String>> baseExtractor,
                                             FeatureExtractor<T, String>... extractors) {
        this(baseExtractor, Arrays.asList(extractors));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<String> extract(T instance) {
        List<String> results = new ArrayList<>();
        for (String result : baseExtractor.extract(instance)) {
            results.add(result + FeatureUtils.CONCAT_DELIM + String.join(FeatureUtils.CONCAT_DELIM, extractors.stream()
                    .map(e -> e.extract(instance))
                    .collect(Collectors.toList())));
        }
        return results;
    }

}
