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

package io.github.clearwsd.feature.optim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.feature.function.AggregateFeatureFunction;
import io.github.clearwsd.feature.function.BiasFeatureFunction;
import io.github.clearwsd.feature.function.FeatureFunction;
import io.github.clearwsd.feature.function.MultiStringFeatureFunction;
import io.github.clearwsd.feature.function.StringFeatureFunction;
import io.github.clearwsd.feature.pipeline.DefaultFeaturePipeline;
import io.github.clearwsd.feature.pipeline.FeaturePipeline;
import io.github.clearwsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Configurable feature pipeline factory.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class NlpFeaturePipelineFactory<I extends NlpInstance, O extends NlpInstance> implements FeaturePipelineFactory<I> {

    private List<FeatureFunctionFactory<I>> factories = new ArrayList<>();

    private Random random;

    public NlpFeaturePipelineFactory(int seed) {
        this.random = new Random(seed);
    }

    @Override
    public FeaturePipeline<I> create() {
        AggregateFeatureFunction<I> featureFunction = new AggregateFeatureFunction<>(factories.stream()
                .map(FeatureFunctionFactory::create)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return new DefaultFeaturePipeline<>(featureFunction);
    }

    public NlpFeaturePipelineFactory<I, O> addBias() {
        factories.add(BiasFeatureFunction::new);
        return this;
    }

    public NlpFeaturePipelineFactory<I, O> addFeatureFunctionFactory(NlpContextFactory<I, O> context,
                                                                     StringExtractor<O> extractor,
                                                                     boolean optional) {
        factories.add(new SingleFeatureFunctionFactory(Collections.singletonList(context), Collections.singletonList(extractor),
                optional));
        return this;
    }

    public NlpFeaturePipelineFactory<I, O> addFeatureFunctionFactory(List<NlpContextFactory<I, O>> contexts,
                                                                     StringExtractor<O> extractor,
                                                                     boolean optional) {
        factories.add(new SingleFeatureFunctionFactory(contexts, Collections.singletonList(extractor), optional));
        return this;
    }

    public NlpFeaturePipelineFactory<I, O> addMultiFeatureFunctionFactory(List<NlpContextFactory<I, O>> contexts,
                                                                          StringListExtractor<O> extractor,
                                                                          boolean optional) {
        factories.add(new ListFeatureFunctionFactory(contexts, Collections.singletonList(extractor), optional));
        return this;
    }

    @AllArgsConstructor
    public class SingleFeatureFunctionFactory implements FeatureFunctionFactory<I> {

        private List<NlpContextFactory<I, O>> contexts;
        private List<StringExtractor<O>> extractors;
        private boolean optional;

        @Override
        public FeatureFunction<I> create() {
            if (optional) {
                if (random.nextInt(contexts.size() + 1) == 0) {
                    return null;
                }
            }
            NlpContextFactory<I, O> contextFactory = contexts.get(random.nextInt(contexts.size()));
            StringExtractor<O> extractor = extractors.get(random.nextInt(extractors.size()));
            return new StringFeatureFunction<>(contextFactory, Collections.singletonList(extractor));
        }
    }

    @AllArgsConstructor
    public class ListFeatureFunctionFactory implements FeatureFunctionFactory<I> {

        private List<NlpContextFactory<I, O>> contexts;
        private List<StringListExtractor<O>> extractors;
        private boolean optional;

        @Override
        public FeatureFunction<I> create() {
            if (optional) {
                if (random.nextInt(contexts.size() + 1) == 0) {
                    return null;
                }
            }
            NlpContextFactory<I, O> contextFactory = contexts.get(random.nextInt(contexts.size()));
            StringListExtractor<O> extractor = extractors.get(random.nextInt(extractors.size()));
            return new MultiStringFeatureFunction<>(contextFactory, Collections.singletonList(extractor));
        }
    }

}
