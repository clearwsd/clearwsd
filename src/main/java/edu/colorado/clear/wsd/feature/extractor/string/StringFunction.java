package edu.colorado.clear.wsd.feature.extractor.string;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.function.Function;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * String function with an ID.
 *
 * @author jamesgung
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@c")
public abstract class StringFunction implements Function<String, String>, Serializable {

    private static final long serialVersionUID = -5951502410231318293L;

    @Getter
    @Accessors(fluent = true)
    protected String id;

}
