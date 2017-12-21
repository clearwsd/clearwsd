package io.github.clearwsd.classifier;

/**
 * Abstract classifier factory.
 *
 * @param <T> classifier type
 * @author jamesgung
 */
public interface ClassifierFactory<T extends Classifier> {

    /**
     * Instantiate a classifier using factory-specific configuration.
     *
     * @return a new classifier
     */
    T create();

}
