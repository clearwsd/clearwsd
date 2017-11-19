package edu.colorodo.clear.wsd.feature.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Interface for lookup-based resources.
 *
 * @param <K> key type
 * @param <T> lookup value type, such as {@link String}.
 * @author jamesgung
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@c")
public interface FeatureResource<K, T> extends Serializable {

    /**
     * Unique identifier of this resource.
     */
    String key();

    /**
     * Lookup a value associated with a key.
     *
     * @param key lookup key
     * @return lookup value
     */
    T lookup(K key);

    /**
     * Initialize this resource.
     *
     * @param inputStream resource
     */
    void initialize(InputStream inputStream);

}
