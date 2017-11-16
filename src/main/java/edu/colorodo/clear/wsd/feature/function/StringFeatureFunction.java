package edu.colorodo.clear.wsd.feature.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.feature.context.NlpContext;
import edu.colorodo.clear.wsd.feature.context.NlpContextFactory;
import edu.colorodo.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorodo.clear.wsd.feature.util.FeatureUtils;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;

/**
 * String feature function.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class StringFeatureFunction<InputT extends NlpInstance, OutputT extends NlpInstance>
        implements FeatureFunction<InputT> {

    private NlpContextFactory<InputT, OutputT> contextFactory;
    private List<FeatureExtractor<OutputT, String>> featureExtractors;

    public StringFeatureFunction(NlpContextFactory<InputT, OutputT> contextFactory,
                                 FeatureExtractor<OutputT, String> featureExtractor) {
        this.contextFactory = contextFactory;
        this.featureExtractors = Collections.singletonList(featureExtractor);
    }

    @Override
    public List<StringFeature> apply(InputT instance) {
        List<StringFeature> features = new ArrayList<>();
        for (NlpContext<OutputT> context : contextFactory.apply(instance)) {
            for (FeatureExtractor<OutputT, String> featureExtractor : featureExtractors) {
                List<String> results = new ArrayList<>();
                for (OutputT token : context.tokens()) {
                    results.add(featureExtractor.extract(token));
                }
                String id = FeatureUtils.computeId(context.identifier(), featureExtractor.id());
                features.add(new StringFeature(id, String.join(FeatureUtils.CONTEXT_DELIM, results)));
            }
        }
        return features;
    }

}
