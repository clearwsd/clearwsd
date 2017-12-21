package io.github.clearwsd.feature.resource;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Resource initializer.
 *
 * @author jamesgung
 */
public interface StringResourceInitializer<T> extends Supplier<T>, Serializable {

}
