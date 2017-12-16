package edu.colorado.clear.wsd.feature.function;

import java.util.ArrayList;
import java.util.List;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.StringFeature;
import edu.colorado.clear.wsd.feature.context.NlpContext;
import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.StringExtractor;
import edu.colorado.clear.wsd.feature.util.FeatureUtils;
import lombok.AllArgsConstructor;

/**
 * String feature function.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class StringFeatureFunction<InputT extends NlpInstance, OutputT extends NlpInstance>
        implements FeatureFunction<InputT> {

    private static final long serialVersionUID = -5155518913022081531L;

    private NlpContextFactory<InputT, OutputT> contextFactory;
    private List<StringExtractor<OutputT>> featureExtractors;

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
