package edu.colorodo.clear.wsd.feature.pipeline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.classifier.Classifier;
import edu.colorodo.clear.wsd.classifier.Hyperparameter;
import edu.colorodo.clear.wsd.classifier.SparseClassifier;
import edu.colorodo.clear.wsd.classifier.SparseInstance;
import edu.colorodo.clear.wsd.feature.model.FeatureModel;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * NLP classifier.
 *
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
        sparseClassifier.train(trainInstances, validInstances);
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

}
