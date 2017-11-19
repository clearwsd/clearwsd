package edu.colorodo.clear.wsd.feature.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import edu.colorodo.clear.wsd.classifier.DefaultStringInstance;
import edu.colorodo.clear.wsd.classifier.SparseInstance;
import edu.colorodo.clear.wsd.classifier.SparseVectorBuilder;
import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.feature.annotator.Annotator;
import edu.colorodo.clear.wsd.feature.function.FeatureFunction;
import edu.colorodo.clear.wsd.feature.model.BaseFeatureModel;
import edu.colorodo.clear.wsd.feature.model.FeatureModel;
import edu.colorodo.clear.wsd.feature.resource.FeatureResourceManager;
import edu.colorodo.clear.wsd.feature.util.VocabularyBuilder;
import edu.colorodo.clear.wsd.type.FeatureType;
import edu.colorodo.clear.wsd.type.NlpInstance;
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
    @JsonProperty
    private Annotator<I> annotators;
    private FeatureModel model;

    private Function<I, String> labelFunction = (i -> i.feature(FeatureType.Gold));
    private transient FeatureResourceManager featureResourceManager;

    public DefaultFeaturePipeline(FeatureResourceManager featureResourceManager) {
        this.featureResourceManager = featureResourceManager;
    }

    public DefaultFeaturePipeline(@JsonProperty("features") FeatureFunction<I> features,
                                  @JsonProperty("annotators") Annotator<I> annotators,
                                  FeatureResourceManager featureResourceManager) {
        this(featureResourceManager);
        this.features = features;
        this.annotators = annotators;
    }

    @Override
    public SparseInstance process(I instance) {
        instance = annotators.annotate(instance);
        List<StringFeature> features = this.features.apply(instance);

        SparseVectorBuilder builder = new SparseVectorBuilder();
        features.stream().map(feature -> model.featureIndex(feature.toString()))
                .forEach(builder::addIndex);

        int target = model.labelIndex(labelFunction.apply(instance));
        return new DefaultStringInstance(instance.index(), target, builder.build());
    }

    @Override
    public List<SparseInstance> train(List<I> instances) {
        model = new BaseFeatureModel();
        annotators.initialize(featureResourceManager);

        VocabularyBuilder featureVocab = new VocabularyBuilder();
        VocabularyBuilder labelVocab = new VocabularyBuilder();

        List<SparseInstance> results = new ArrayList<>();
        for (I instance : instances) {
            instance = annotators.annotate(instance);
            List<StringFeature> features = this.features.apply(instance);

            SparseVectorBuilder builder = new SparseVectorBuilder();
            features.stream().map(f -> featureVocab.index(f.toString()))
                    .forEach(builder::addIndex);

            int target = labelVocab.index(labelFunction.apply(instance));
            results.add(new DefaultStringInstance(instance.index(), target, builder.build()));
        }

        model.features(featureVocab.build());
        model.labels(labelVocab.build());

        return results;
    }

}
