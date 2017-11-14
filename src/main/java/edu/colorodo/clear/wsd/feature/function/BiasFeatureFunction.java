package edu.colorodo.clear.wsd.feature.function;

import java.util.Collections;
import java.util.List;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Setter;

/**
 * Bias feature function (always applied).
 *
 * @author jamesgung
 */
public class BiasFeatureFunction<T extends NlpInstance> implements FeatureFunction<T> {

    public static final String BIAS = "<BIAS>";

    @Setter
    private List<StringFeature> bias = Collections.singletonList(new StringFeature(BIAS, BIAS));

    @Override
    public List<StringFeature> apply(T input) {
        return bias;
    }
}
