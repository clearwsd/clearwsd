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

package io.github.clearwsd.feature.annotator;

import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.resource.FeatureResourceManager;
import lombok.AllArgsConstructor;

/**
 * Composite annotator (applies mulitple annotators sequentially).
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class AggregateAnnotator<S extends NlpInstance> implements Annotator<S> {

    private static final long serialVersionUID = -3218243259530880592L;

    private List<Annotator<S>> annotators;

    @Override
    public S annotate(S instance) {
        for (Annotator<S> annotator : annotators) {
            instance = annotator.annotate(instance);
        }
        return instance;
    }

    @Override
    public boolean initialized() {
        return annotators.stream().allMatch(Annotator::initialized);
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        for (Annotator<S> annotator : annotators) {
            annotator.initialize(featureResourceManager);
        }
    }
}
