package edu.colorodo.clear.wsd.feature.pipeline;

import com.google.common.base.Preconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import edu.colorodo.clear.wsd.classifier.DefaultStringInstance;
import edu.colorodo.clear.wsd.classifier.SparseVectorBuilder;
import edu.colorodo.clear.wsd.classifier.StringInstance;
import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.feature.annotator.Annotator;
import edu.colorodo.clear.wsd.feature.function.FeatureFunction;
import edu.colorodo.clear.wsd.feature.model.FeatureModel;
import edu.colorodo.clear.wsd.feature.util.VocabularyBuilder;
import edu.colorodo.clear.wsd.type.FeatureType;
import edu.colorodo.clear.wsd.type.NlpInstance;
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

    @JsonProperty
    private FeatureFunction<I> featureFunction;
    @JsonProperty
    private List<Annotator<I>> annotators;
    private Function<I, String> labelFunction = (i -> i.feature(FeatureType.Gold));

    private FeatureModel model;

    public DefaultFeaturePipeline(@JsonProperty("featureFunction") FeatureFunction<I> featureFunction,
                                  @JsonProperty("annotators") List<Annotator<I>> annotators) {
        this.featureFunction = featureFunction;
        this.annotators = annotators;
    }

    @Override
    public void initialize(FeatureModel featureModel) {
        this.model = featureModel;
    }

    @Override
    public StringInstance process(I instance) {
        for (Annotator<I> annotator : annotators) {
            instance = annotator.annotate(instance);
        }
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
            for (Annotator<I> annotator : annotators) {
                instance = annotator.annotate(instance);
            }
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
