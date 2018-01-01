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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.util.FeatureUtils;

/**
 * Feature extractor concatenating the results of multiple different extractors. Concatenates resulting features.
 *
 * @author jamesgung
 */
public class ConcatenatingFeatureExtractor<T extends NlpInstance> implements StringExtractor<T> {

    private static final long serialVersionUID = 2179984341749253937L;

    private List<FeatureExtractor<T, String>> extractors;
    private String id;

    public ConcatenatingFeatureExtractor(List<FeatureExtractor<T, String>> extractors) {
        this.extractors = extractors;
        id = extractors.stream().map(FeatureExtractor::id).collect(Collectors.joining(FeatureUtils.KEY_DELIM));
    }

    @SafeVarargs
    public ConcatenatingFeatureExtractor(FeatureExtractor<T, String>... extractors) {
        this(Arrays.asList(extractors));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String extract(T instance) {
        return String.join(FeatureUtils.CONCAT_DELIM, extractors.stream()
                .map(e -> e.extract(instance))
                .collect(Collectors.toList()));
    }

}
