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

import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import io.github.clearwsd.feature.context.NlpContext;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.StringExtractor;
import io.github.clearwsd.feature.util.FeatureUtils;
import lombok.AllArgsConstructor;

/**
 * String feature function.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class StringFeatureFunction<InputT extends NlpInstance, OutputT extends NlpInstance>
        implements FeatureFunction<InputT> {

    private static final long serialVersionUID = -5155518913022081531L;

    private NlpContextFactory<InputT, OutputT> contextFactory;
    private List<StringExtractor<OutputT>> featureExtractors;

    @Override
    public List<StringFeature> apply(InputT instance) {
        List<StringFeature> features = new ArrayList<>();
        for (NlpContext<OutputT> context : contextFactory.apply(instance)) {
            for (FeatureExtractor<OutputT, String> featureExtractor : featureExtractors) {
                List<String> results = new ArrayList<>();
                for (OutputT token : context.tokens()) {
                    results.add(featureExtractor.extract(token));
                }
                String id = FeatureUtils.computeId(context.identifier(), featureExtractor.id());
                features.add(new StringFeature(id, String.join(FeatureUtils.CONTEXT_DELIM, results)));
            }
        }
        return features;
    }

}
