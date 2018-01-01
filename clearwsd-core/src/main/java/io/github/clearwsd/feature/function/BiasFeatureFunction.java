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

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Bias feature function (always applied).
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class BiasFeatureFunction<T extends NlpInstance> implements FeatureFunction<T> {

    private static final String BIAS = "<BIAS>";

    private static final long serialVersionUID = -2067474355880710744L;

    private List<StringFeature> bias = Collections.singletonList(new StringFeature(BIAS, BIAS));

    @Override
    public List<StringFeature> apply(T input) {
        return bias;
    }
}
