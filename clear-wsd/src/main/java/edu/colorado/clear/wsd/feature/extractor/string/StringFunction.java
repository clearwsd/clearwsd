package edu.colorado.clear.wsd.feature.extractor.string;

import java.io.Serializable;
import java.util.function.Function;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * String function with an ID.
 *
 * @author jamesgung
 */
public abstract class StringFunction implements Function<String, String>, Serializable {

    private static final long serialVersionUID = -5951502410231318293L;

    @Getter
    @Accessors(fluent = true)
    protected String id;

}
