package edu.colorodo.clear.wsd.classifier;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Classification algorithm over sparse data.
 *
 * @param <T> model type
 * @author jamesgung
 */
public interface SparseClassifier<T extends ModelParameters> {

    /**
     * Returns the current model parameters.
     */
    T parameters();

    /**
     * Initializes this model with provided parameters.
     *
     * @param parameters model parameters
     */
    void initialize(T parameters);

    /**
     * Classify the input instance.
     *
     * @param instance sparse input instance
     * @return String label
     */
    String classify(SparseVector instance);

    /**
     * Produce confidence scores or probabilities for each label given an input instance.
     *
     * @param instance input instance
     * @return map from labels to scores
     */
    Map<String, Double> score(SparseVector instance);

    /**
     * Trains a new model, overwriting any existing model.
     */
    T train(List<StringInstance> train, List<StringInstance> valid);

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
     * Initialize model parameters from a given {@link InputStream}.
     *
     * @param inputStream model parameters input stream
     */
    void load(InputStream inputStream);

    /**
     * Save model parameters to a given {@link OutputStream}.
     *
     * @param outputStream model parameters output stream
     */
    void save(OutputStream outputStream);
}
