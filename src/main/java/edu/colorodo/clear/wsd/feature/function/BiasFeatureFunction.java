package edu.colorodo.clear.wsd.feature.function;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Bias feature function (always applied).
 *
 * @author jamesgung
 */
@NoArgsConstructor
@AllArgsConstructor
public class BiasFeatureFunction<T extends NlpInstance> implements FeatureFunction<T> {

    public static final String BIAS = "<BIAS>";

    @JsonProperty
    private List<StringFeature> bias = Collections.singletonList(new StringFeature(BIAS, BIAS));

    @Override
    public List<StringFeature> apply(T input) {
        return bias;
    }
}
