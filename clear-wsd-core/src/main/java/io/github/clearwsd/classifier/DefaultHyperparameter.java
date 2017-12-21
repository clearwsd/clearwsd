package io.github.clearwsd.classifier;

import java.util.function.BiConsumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Default hyperparameter implementation.
 *
 * @author jamesgung
 */
@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class DefaultHyperparameter<T> implements Hyperparameter<T> {

    private String name;
    private String key;
    private String description;
    private String defaultValue;
    private BiConsumer<T, String> assign;

    @Override
    public void assignValue(T model, String value) {
        assign.accept(model, value);
    }

}
