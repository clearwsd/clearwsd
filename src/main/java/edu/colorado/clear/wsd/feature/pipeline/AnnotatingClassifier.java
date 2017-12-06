package edu.colorado.clear.wsd.feature.pipeline;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.Hyperparameter;
import edu.colorado.clear.wsd.feature.annotator.Annotator;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;

/**
 * Classifier wrapper that applies provided annotations prior to training, classification and scoring.
 *
 * @author jamesgung
 */
public class AnnotatingClassifier<T> implements Classifier<T, String> {

    private static final long serialVersionUID = -6677942446282205271L;

    private Classifier<T, String> classifier;
    private Annotator<T> annotator;

    public AnnotatingClassifier(Classifier<T, String> classifier,
                                Annotator<T> annotator) {
        this.classifier = classifier;
        this.annotator = annotator;
    }

    public void initialize(FeatureResourceManager featureResourceManager) {
        annotator.initialize(featureResourceManager);
    }

    @Override
    public String classify(T instance) {
        Preconditions.checkState(annotator.initialized(), "Annotator is not initialized.");
        instance = annotator.annotate(instance);
        return classifier.classify(instance);
    }

    @Override
    public Map<String, Double> score(T instance) {
        Preconditions.checkState(annotator.initialized(), "Annotator is not initialized.");
        instance = annotator.annotate(instance);
        return classifier.score(instance);
    }

    @Override
    public void train(List<T> train, List<T> valid) {
        Preconditions.checkState(annotator.initialized(), "Annotator is not initialized.");
        train = train.stream().map(annotator::annotate).collect(Collectors.toList());
        valid = valid.stream().map(annotator::annotate).collect(Collectors.toList());
        classifier.train(train, valid);
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return classifier.hyperparameters();
    }

    @Override
    public void initialize(Properties properties) {
        classifier.initialize(properties);
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            //noinspection unchecked
            classifier = (Classifier<T, String>) inputStream.readObject();
            //noinspection unchecked
            annotator = (Annotator<T>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
            outputStream.writeObject(annotator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
