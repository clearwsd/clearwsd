package edu.colorodo.clear.wsd.feature.pipeline;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import edu.colorodo.clear.wsd.classifier.DefaultStringInstance;
import edu.colorodo.clear.wsd.classifier.SparseVectorBuilder;
import edu.colorodo.clear.wsd.classifier.StringInstance;
import edu.colorodo.clear.wsd.feature.function.FeatureFunction;
import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.feature.util.VocabularyBuilder;
import edu.colorodo.clear.wsd.feature.model.FeatureModel;
import edu.colorodo.clear.wsd.type.FeatureType;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Base feature pipeline.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class BaseFeaturePipeline<I extends NlpInstance> implements FeaturePipeline<I> {

    private FeatureFunction<I> featureFunction;
    private Function<I, String> labelFunction = (i -> i.feature(FeatureType.Gold));

    private FeatureModel model;

    public BaseFeaturePipeline(FeatureFunction<I> featureFunction) {
        this.featureFunction = featureFunction;
    }

    @Override
    public void initialize(FeatureModel featureModel) {
        this.model = featureModel;
    }

    @Override
    public StringInstance process(I instance) {
        List<StringFeature> features = featureFunction.apply(instance);

        SparseVectorBuilder builder = new SparseVectorBuilder();
        features.stream().map(f -> model.featureIndex(f.toString()))
                .forEach(builder::addIndex);

        int target = model.labelIndex(labelFunction.apply(instance));
        return new DefaultStringInstance(instance.index(), target, builder.build());
    }

    @Override
    public List<StringInstance> train(List<I> instances) {
        Preconditions.checkNotNull(model, "Model must not be null. Initialize model before training.");

        VocabularyBuilder featureVocab = new VocabularyBuilder();
        VocabularyBuilder labelVocab = new VocabularyBuilder();

        List<StringInstance> results = new ArrayList<>();
        for (I instance : instances) {
            List<StringFeature> features = featureFunction.apply(instance);

            SparseVectorBuilder builder = new SparseVectorBuilder();
            features.stream().map(f -> featureVocab.index(f.toString()))
                    .forEach(builder::addIndex);

            int target = labelVocab.index(labelFunction.apply(instance));
            results.add(new DefaultStringInstance(instance.index(), target, builder.build()));
        }

        model.features(featureVocab.build())
                .labels(labelVocab.build());

        return results;
    }

}
