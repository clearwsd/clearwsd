package edu.colorodo.clear.wsd.feature.function;

import java.util.ArrayList;
import java.util.List;

import edu.colorodo.clear.wsd.feature.StringFeature;
import edu.colorodo.clear.wsd.feature.context.NlpContext;
import edu.colorodo.clear.wsd.feature.context.NlpContextFactory;
import edu.colorodo.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorodo.clear.wsd.feature.util.FeatureUtils;
import edu.colorodo.clear.wsd.type.NlpInstance;

/**
 * Default feature function implementation.
 *
 * @author jamesgung
 */
public class DefaultFeatureFunction<InputT extends NlpInstance, OutputT extends NlpInstance> implements FeatureFunction<InputT> {

    private NlpContextFactory<InputT, OutputT> contextFactory;
    private FeatureExtractor<OutputT> featureExtractor;

    public DefaultFeatureFunction(NlpContextFactory<InputT, OutputT> contextFactory, FeatureExtractor<OutputT> featureExtractor) {
        this.contextFactory = contextFactory;
        this.featureExtractor = featureExtractor;
    }

    @Override
    public List<StringFeature> apply(InputT instance) {
        List<StringFeature> features = new ArrayList<>();
        for (NlpContext<OutputT> context : contextFactory.apply(instance)) {
            List<String> results = new ArrayList<>();
            for (OutputT token : context.tokens()) {
                results.add(featureExtractor.extract(token));
            }
            String id = FeatureUtils.computeId(context.identifier(), featureExtractor.id());
            features.add(new StringFeature(id, String.join(FeatureUtils.CONTEXT_DELIM, results)));
        }
        return features;
    }

}
