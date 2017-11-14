package edu.colorodo.clear.wsd.feature.extractor.string;

import java.util.function.Function;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * String function with an ID.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public abstract class StringFunction implements Function<String, String> {

    @Getter
    protected String id;

}
