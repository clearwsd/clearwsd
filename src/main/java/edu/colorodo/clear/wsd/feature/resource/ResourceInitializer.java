package edu.colorodo.clear.wsd.feature.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.InputStream;
import java.util.function.BiConsumer;

/**
 * Resource initializer.
 *
 * @author jamesgung
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@c")
public interface ResourceInitializer<T> extends BiConsumer<T, InputStream>{

}
