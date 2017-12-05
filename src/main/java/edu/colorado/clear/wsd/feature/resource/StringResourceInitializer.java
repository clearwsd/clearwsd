package edu.colorado.clear.wsd.feature.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Resource initializer.
 *
 * @author jamesgung
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@c")
public interface StringResourceInitializer<T> extends Supplier<T>, Serializable {

}
