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
