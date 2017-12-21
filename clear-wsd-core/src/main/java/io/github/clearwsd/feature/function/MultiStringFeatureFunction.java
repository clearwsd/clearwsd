package io.github.clearwsd.feature.function;

import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.StringListExtractor;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.feature.StringFeature;
import io.github.clearwsd.feature.context.NlpContext;
import io.github.clearwsd.feature.context.NlpContextFactory;
import io.github.clearwsd.feature.util.FeatureUtils;
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

    private static final long serialVersionUID = 2326594845630574435L;

    private NlpContextFactory<InputT, OutputT> contextFactory;
    private List<StringListExtractor<OutputT>> featureExtractors;

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
