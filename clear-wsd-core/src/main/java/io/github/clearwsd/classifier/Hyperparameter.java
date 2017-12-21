package io.github.clearwsd.classifier;

/**
 * Parameter/setting chosen prior to learning (not learned during training).
 *
 * @author jamesgung
 */
public interface Hyperparameter<T> {

    /**
     * Readable identifier.
     */
    String name();

    /**
     * Unique identifier.
     */
    String key();

    /**
     * Description of parameter.
     */
    String description();

    /**
     * Default value of parameter when none is provided.
     */
    String defaultValue();

    /**
     * Parse and assign the value to a model.
     *
     * @param model target model
     * @param value input value
     */
    void assignValue(T model, String value);

}
