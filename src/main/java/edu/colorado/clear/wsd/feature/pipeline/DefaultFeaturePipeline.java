package edu.colorado.clear.wsd.feature.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import edu.colorado.clear.wsd.classifier.DefaultSparseInstance;
import edu.colorado.clear.wsd.classifier.SparseInstance;
import edu.colorado.clear.wsd.classifier.SparseVectorBuilder;
import edu.colorado.clear.wsd.feature.StringFeature;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.model.BaseFeatureModel;
import edu.colorado.clear.wsd.feature.model.FeatureModel;
import edu.colorado.clear.wsd.feature.util.VocabularyBuilder;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default feature pipeline. Feature configuration is JSON-serializable through Jackson.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class DefaultFeaturePipeline<I extends NlpInstance> implements FeaturePipeline<I> {

    private static final long serialVersionUID = 7756681760870831311L;

    @JsonProperty
    private FeatureFunction<I> features;
    private FeatureModel model;

    private Function<I, String> labelFunction = (Serializable & Function<I, String>) i -> i.feature(FeatureType.Gold);

    public DefaultFeaturePipeline(@JsonProperty("features") FeatureFunction<I> features) {
        this.features = features;
    }

    @Override
    public SparseInstance process(I instance) {
        List<StringFeature> features = this.features.apply(instance);

        SparseVectorBuilder builder = new SparseVectorBuilder();
        features.stream().map(feature -> model.featureIndex(feature.toString()))
                .forEach(builder::addIndex);

        int target = model.labelIndex(instance.feature(FeatureType.Gold));
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

            int target = labelVocab.index(instance.feature(FeatureType.Gold));
            results.add(new DefaultSparseInstance(instance.index(), target, builder.build()));
        }

        model.features(featureVocab.build());
        model.labels(labelVocab.build());

        return results;
    }

}
