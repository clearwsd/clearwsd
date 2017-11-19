package edu.colorodo.clear.wsd.classifier;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Classification algorithm.
 *
 * @param <U> input type
 * @param <O> output type
 * @author jamesgung
 */
public interface Classifier<U, O> extends Serializable {

    /**
     * Classify the input instance.
     *
     * @param instance input instance
     * @return String label
     */
    O classify(U instance);

    /**
     * Produce confidence scores or probabilities for each label given an input instance.
     *
     * @param instance input instance
     * @return map from labels to scores
     */
    Map<O, Double> score(U instance);

    /**
     * Trains a new model, overwriting any existing model.
     */
    void train(List<U> train, List<U> valid);

    /**
     * Return the list of {@link Hyperparameter Hyperparameter(s)} associated with this classifier.
     *
     * @return list of {@link Hyperparameter Hyperparameter(s)}.
     */
    List<Hyperparameter> hyperparameters();

    /**
     * Initialize {@link Hyperparameter Hyperparameter(s)} of this classifier given a {@link Properties} object.
     * If a property is not filled, use the default value for the associated hyperparameter.
     *
     * @param properties hyperparameter values
     */
    default void initialize(Properties properties) {
        for (Hyperparameter property : hyperparameters()) {
            if (properties.containsKey(property.name())) {
                //noinspection unchecked
                property.assignValue(this, properties.getProperty(property.name()));
            } else {
                //noinspection unchecked
                property.assignValue(this, property.defaultValue());
            }
        }
    }

    /**
     * Initialize classifier from a given {@link ObjectInputStream}. Input should not be closed within implementation.
     *
     * @param inputStream object input stream
     */
    void load(ObjectInputStream inputStream);

    /**
     * Save classifier to a given {@link ObjectOutputStream}. Output stream should not be closed within implementation, but is
     * managed by caller.
     *
     * @param outputStream object output stream
     */
    void save(ObjectOutputStream outputStream);

}
