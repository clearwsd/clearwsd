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
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Aggregation of multiple feature functions.
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class AggregateFeatureFunction<InputT extends NlpInstance> implements FeatureFunction<InputT> {

    private static final long serialVersionUID = 7273553475535366584L;

    private List<FeatureFunction<InputT>> functions = new ArrayList<>();

    public AggregateFeatureFunction<InputT> add(FeatureFunction<InputT> function) {
        functions.add(function);
        return this;
    }

    @Override
    public List<StringFeature> apply(InputT input) {
        return functions.stream()
                .map(f -> f.apply(input))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
