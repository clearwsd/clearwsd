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

package io.github.clearwsd.feature.function;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;

/**
 * Feature function utilities.
 *
 * @author jamesgung
 */
public class Features {

    private Features() {
    }

    public static <I extends NlpInstance, T extends NlpInstance, U> FeatureFunction<I> function(
            NlpContextFactory<I, T> context, List<? extends FeatureExtractor<T, U>> extractors) {
        if (extractors.size() == 0) {
            throw new IllegalArgumentException("Feature function requires at least one extractor, got 0.");
        }
        FeatureExtractor<T, U> extractor = extractors.get(0);
        if (extractor instanceof StringExtractor) {
            List<StringExtractor<T>> stringExtractors = extractors.stream()
                    .map(e -> ((StringExtractor<T>) e))
                    .collect(Collectors.toList());
            return new StringFeatureFunction<>(context, stringExtractors);
        } else if (extractor instanceof StringListExtractor) {
            List<StringListExtractor<T>> stringListExtractors = extractors.stream()
                    .map(e -> ((StringListExtractor<T>) e))
                    .collect(Collectors.toList());
            return new MultiStringFeatureFunction<>(context, stringListExtractors);
        }
        throw new IllegalArgumentException("Unsupported extractor :" + extractors.getClass());
    }

    public static <I extends NlpInstance, T extends NlpInstance, U> FeatureFunction<I> function(NlpContextFactory<I, T> context,
                                                                                                FeatureExtractor<T, U> extractor) {
        return function(context, Collections.singletonList(extractor));
    }

    public static <T extends NlpInstance> ConjunctionFunction<T> cross(FeatureFunction<T> first, FeatureFunction<T> second) {
        return new ConjunctionFunction<>(first, second);
    }

    public static <T extends NlpInstance> ConjunctionFunction<T> cross(FeatureFunction<T> self) {
        return new ConjunctionFunction<>(self, self);
    }

    public static <T extends NlpInstance> BiasFeatureFunction<T> bias() {
        return new BiasFeatureFunction<>();
    }

}
