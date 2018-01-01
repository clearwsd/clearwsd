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

import java.io.Serializable;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;

/**
 * Feature function that produces a list of features given a context and an extractor.
 *
 * @param <InputT> input type
 * @author jamesgung
 */
public interface FeatureFunction<InputT extends NlpInstance> extends Serializable {

    /**
     * Given an NLP instance type, produce a corresponding list of categorical features.
     *
     * @param input input NLP instance
     * @return list of string features
     */
    List<StringFeature> apply(InputT input);

}
