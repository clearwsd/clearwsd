package edu.colorado.clear.wsd.feature.pipeline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.DefaultSparseInstance;
import edu.colorado.clear.wsd.classifier.DefaultSparseVector;
import edu.colorado.clear.wsd.classifier.DummyClassifier;
import edu.colorado.clear.wsd.classifier.Hyperparameter;
import edu.colorado.clear.wsd.classifier.SparseClassifier;
import edu.colorado.clear.wsd.classifier.SparseInstance;
import edu.colorado.clear.wsd.feature.model.BaseFeatureModel;
import edu.colorado.clear.wsd.feature.model.BaseVocabulary;
import edu.colorado.clear.wsd.feature.model.FeatureModel;
import edu.colorado.clear.wsd.feature.util.VocabularyBuilder;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * NLP classifier that includes a {@link FeaturePipeline} used to convert
 * each input to a {@link SparseInstance} for input to a {@link SparseClassifier}.
 * If training data only contains one label, trains a dummy classifier that always predicts
 * that label rather than going through the full training process.
 *
 * @param <U> input type
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class NlpClassifier<U extends NlpInstance> implements Classifier<U, String> {

    private static final long serialVersionUID = -7706241433014645184L;

    private SparseClassifier sparseClassifier;
    private FeaturePipeline<U> featurePipeline;

    public NlpClassifier(SparseClassifier sparseClassifier, FeaturePipeline<U> featurePipeline) {
        this.sparseClassifier = sparseClassifier;
        this.featurePipeline = featurePipeline;
    }

    @Override
    public String classify(U instance) {
        return featurePipeline.model().label(sparseClassifier.classify(featurePipeline.process(instance)));
    }

    @Override
    public Map<String, Double> score(U instance) {
        FeatureModel model = featurePipeline.model();
        return sparseClassifier.score(featurePipeline.process(instance)).entrySet().stream()
                .collect(Collectors.toMap(e -> model.label(e.getKey()), Map.Entry::getValue));
    }

    @Override
    public void train(List<U> train, List<U> valid) {
        List<SparseInstance> trainInstances = featurePipeline.train(train);
        List<SparseInstance> validInstances = valid.stream()
                .map(featurePipeline::process)
                .collect(Collectors.toList());
        if (featurePipeline.model().labels().indices().size() >= 2) {
            sparseClassifier.train(trainInstances, validInstances);
        } else {
            featurePipeline = new DummyPipeline<>(featurePipeline.model().labels().value(0));
            sparseClassifier = new DummyClassifier(0);
        }
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return sparseClassifier.hyperparameters();
    }

    @Override
    public void initialize(Properties properties) {
        sparseClassifier.initialize(properties);
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            sparseClassifier = (SparseClassifier) inputStream.readObject();
            //noinspection unchecked
            featurePipeline = (FeaturePipeline<U>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(sparseClassifier);
            outputStream.writeObject(featurePipeline);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DummyPipeline<I extends NlpInstance> implements FeaturePipeline<I> {

        private static final long serialVersionUID = -1320433214836264964L;

        private FeatureModel model;

        DummyPipeline(String label) {
            VocabularyBuilder builder = new VocabularyBuilder();
            builder.index(label);
            this.model = new BaseFeatureModel(builder.build(), new BaseVocabulary(new HashMap<>()));
        }

        @Override
        public FeatureModel model() {
            return model;
        }

        @Override
        public SparseInstance process(I inputInstance) {
            return new DefaultSparseInstance(0, 0, new DefaultSparseVector(new int[]{0}, new float[]{1}));
        }

        @Override
        public List<SparseInstance> train(List<I> instances) {
            throw new UnsupportedOperationException();
        }

    }

}
