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

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import io.github.clearwsd.feature.util.FeatureUtils;
import lombok.AllArgsConstructor;

/**
 * Conjoin the results of two feature functions.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class ConjunctionFunction<InputT extends NlpInstance> implements FeatureFunction<InputT> {

    private static final long serialVersionUID = 218606640626375039L;

    private FeatureFunction<InputT> first;
    private FeatureFunction<InputT> second;

    @Override
    public List<StringFeature> apply(InputT input) {
        List<StringFeature> results = new ArrayList<>();
        for (StringFeature first : first.apply(input)) {
            for (StringFeature second : second.apply(input)) {
                if (first.equals(second)) {
                    continue;
                }
                results.add(new StringFeature(first.id() + FeatureUtils.CONCAT_DELIM + second.id(),
                        first.value() + FeatureUtils.CONCAT_DELIM + second.value()));
            }
        }
        return results;
    }

}
