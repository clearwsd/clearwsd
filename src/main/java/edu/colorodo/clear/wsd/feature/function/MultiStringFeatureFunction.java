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
import lombok.extern.slf4j.Slf4j;

/**
 * Multi-string feature function. Computes features for each instance in a context independently, so should not be applied over
 * multi-token contexts.
 *
 * @author jamesgung
 */
@Slf4j
@AllArgsConstructor
public class MultiStringFeatureFunction<InputT extends NlpInstance, OutputT extends NlpInstance>
        implements FeatureFunction<InputT> {

    private NlpContextFactory<InputT, OutputT> contextFactory;
    private List<FeatureExtractor<OutputT, List<String>>> featureExtractors;

    public MultiStringFeatureFunction(NlpContextFactory<InputT, OutputT> contextFactory,
                                      FeatureExtractor<OutputT, List<String>> featureExtractor) {
        this.contextFactory = contextFactory;
        this.featureExtractors = Collections.singletonList(featureExtractor);
    }

    @Override
    public List<StringFeature> apply(InputT instance) {
        List<StringFeature> features = new ArrayList<>();
        for (NlpContext<OutputT> context : contextFactory.apply(instance)) {
            if (context.tokens().size() > 1) {
                log.warn("Warning: using a multi-string feature extractor for a multi-token context.");
            }
            for (FeatureExtractor<OutputT, List<String>> featureExtractor : featureExtractors) {
                String id = FeatureUtils.computeId(context.identifier(), featureExtractor.id());
                for (OutputT token : context.tokens()) {
                    for (String result : featureExtractor.extract(token)) {
                        features.add(new StringFeature(id, result));
                    }
                }
            }
        }
        return features;
    }

}
