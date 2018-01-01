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

package io.github.clearwsd.feature.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.clearwsd.classifier.DefaultSparseInstance;
import io.github.clearwsd.classifier.SparseInstance;
import io.github.clearwsd.classifier.SparseVectorBuilder;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.classifier.DefaultSparseInstance;
import io.github.clearwsd.classifier.SparseInstance;
import io.github.clearwsd.classifier.SparseVectorBuilder;
import io.github.clearwsd.feature.StringFeature;
import io.github.clearwsd.feature.function.FeatureFunction;
import io.github.clearwsd.feature.model.BaseFeatureModel;
import io.github.clearwsd.feature.model.FeatureModel;
import io.github.clearwsd.feature.util.VocabularyBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default feature pipeline.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class DefaultFeaturePipeline<I extends NlpInstance> implements FeaturePipeline<I> {

    private static final long serialVersionUID = 7756681760870831311L;

    private FeatureFunction<I> features;
    private FeatureModel model;

    private Function<I, String> labelFunction = (Serializable & Function<I, String>) i -> i.feature(FeatureType.Gold);

    public DefaultFeaturePipeline(FeatureFunction<I> features) {
        this.features = features;
    }

    @Override
    public SparseInstance process(I instance) {
        List<StringFeature> features = this.features.apply(instance);

        SparseVectorBuilder builder = new SparseVectorBuilder();
        features.stream().map(feature -> model.featureIndex(feature.toString()))
                .forEach(builder::addIndex);

        int target = model.labelIndex(labelFunction.apply(instance));
        return new DefaultSparseInstance(instance.index(), target, builder.build());
    }

    @Override
    public List<SparseInstance> train(List<I> instances) {
        model = new BaseFeatureModel();

        VocabularyBuilder featureVocab = new VocabularyBuilder();
        VocabularyBuilder labelVocab = new VocabularyBuilder();

        List<SparseInstance> results = new ArrayList<>();
        for (I instance : instances) {
            List<StringFeature> features = this.features.apply(instance);

            SparseVectorBuilder builder = new SparseVectorBuilder();
            features.stream().map(f -> featureVocab.index(f.toString()))
                    .forEach(builder::addIndex);

            int target = labelVocab.index(labelFunction.apply(instance));
            results.add(new DefaultSparseInstance(instance.index(), target, builder.build()));
        }

        model.features(featureVocab.build());
        model.labels(labelVocab.build());

        return results;
    }

}
