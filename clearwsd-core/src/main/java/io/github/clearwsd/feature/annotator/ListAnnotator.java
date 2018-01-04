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

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;
import io.github.clearwsd.feature.context.NlpContext;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.context.SequenceIdentifyContextFactory;
import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.resource.FeatureResourceManager;

/**
 * Annotates tokens in a sequence with lists.
 *
 * @author jamesgung
 */
public class ListAnnotator<T extends NlpInstance, S extends NlpSequence<T>> extends ResourceAnnotator<String, S> {

    private static final long serialVersionUID = -6170529305032382231L;

    private FeatureExtractor<T, String> baseExtractor;
    private NlpContextFactory<S, T> contextFactory;

    public ListAnnotator(String resourceKey,
                         FeatureExtractor<T, String> baseExtractor,
                         NlpContextFactory<S, T> contextFactory) {
        super(resourceKey);
        this.baseExtractor = baseExtractor;
        this.contextFactory = contextFactory;
    }

    public ListAnnotator(String resourceKey, FeatureExtractor<T, String> baseExtractor) {
        this(resourceKey, baseExtractor, new SequenceIdentifyContextFactory<>());
        this.baseExtractor = baseExtractor;
    }

    @Override
    public S annotate(S instance) {
        for (NlpContext<T> context : contextFactory.apply(instance)) {
            for (T token : context.tokens()) {
                String key = baseExtractor.extract(token);
                token.addFeature(resourceKey, resource.lookup(key));
            }
        }
        return instance;
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        this.resource = featureResourceManager.getResource(resourceKey);
    }

}
