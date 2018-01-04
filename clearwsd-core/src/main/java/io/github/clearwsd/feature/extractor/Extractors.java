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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.extractor.string.LowercaseFunction;

/**
 * Utilities for simple instantiation of commonly-used extractors.
 *
 * @author jamesgung
 */
public class Extractors {

    private Extractors() {
    }

    public static <T extends NlpInstance> LookupFeatureExtractor<T> lookup(FeatureType featureType) {
        //noinspection unchecked
        return (LookupFeatureExtractor<T>) new LookupFeatureExtractor<>(featureType.name());
    }

    public static <T extends NlpInstance> ListLookupFeatureExtractor<T> listLookup(Collection<String> keys) {
        return new ListLookupFeatureExtractor<>(new ArrayList<>(keys));
    }

    public static <T extends NlpInstance> ListLookupFeatureExtractor<T> listLookup(String... keys) {
        return new ListLookupFeatureExtractor<>(Arrays.asList(keys));
    }

    public static <T extends NlpInstance> LookupFeatureExtractor<T> form() {
        return lookup(FeatureType.Text);
    }

    public static <T extends NlpInstance> LookupFeatureExtractor<T> lemma() {
        return lookup(FeatureType.Lemma);
    }

    public static <T extends NlpInstance> StringFunctionExtractor<T> lowerForm() {
        return new StringFunctionExtractor<>(form(), new LowercaseFunction());
    }

    public static <T extends NlpInstance> StringFunctionExtractor<T> lowerLemma() {
        return new StringFunctionExtractor<>(lemma(), new LowercaseFunction());
    }

    @SafeVarargs
    public static <T extends NlpInstance> ConcatenatingFeatureExtractor<T> concat(FeatureExtractor<T, String>... extractors) {
        return new ConcatenatingFeatureExtractor<>(extractors);
    }

    public static <T extends NlpInstance> List<StringExtractor<T>> concat(FeatureExtractor<T, String> extractor,
                                                                          List<FeatureExtractor<T, String>> extractors) {
        return extractors.stream().map(e -> concat(e, extractor)).collect(Collectors.toList());
    }

    public static <T extends NlpInstance> ListConcatenatingFeatureExtractor<T> listConcat(StringListExtractor<T> base,
                                                                                          StringExtractor<T> extractors) {
        return new ListConcatenatingFeatureExtractor<>(base, extractors);
    }

    public static <T extends NlpInstance> List<StringListExtractor<T>> listConcat(StringExtractor<T> extractor,
                                                                                  List<StringListExtractor<T>> extractors) {
        return extractors.stream().map(e -> listConcat(e, extractor)).collect(Collectors.toList());
    }

}
