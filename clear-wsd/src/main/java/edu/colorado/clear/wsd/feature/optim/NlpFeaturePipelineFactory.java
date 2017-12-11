package edu.colorado.clear.wsd.feature.optim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.feature.context.NlpContextFactory;
import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.function.AggregateFeatureFunction;
import edu.colorado.clear.wsd.feature.function.BiasFeatureFunction;
import edu.colorado.clear.wsd.feature.function.FeatureFunction;
import edu.colorado.clear.wsd.feature.function.MultiStringFeatureFunction;
import edu.colorado.clear.wsd.feature.function.StringFeatureFunction;
import edu.colorado.clear.wsd.feature.pipeline.DefaultFeaturePipeline;
import edu.colorado.clear.wsd.feature.pipeline.FeaturePipeline;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Configurable feature pipeline factory.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class NlpFeaturePipelineFactory<I extends NlpInstance, O extends NlpInstance> implements FeaturePipelineFactory<I> {

    private List<FeatureFunctionFactory<I>> factories = new ArrayList<>();

    private Random random;

    public NlpFeaturePipelineFactory(int seed) {
        this.random = new Random(seed);
    }

    @Override
    public FeaturePipeline<I> create() {
        AggregateFeatureFunction<I> featureFunction = new AggregateFeatureFunction<>(factories.stream()
                .map(FeatureFunctionFactory::create)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return new DefaultFeaturePipeline<>(featureFunction);
    }

    public NlpFeaturePipelineFactory<I, O> addBias() {
        factories.add(BiasFeatureFunction::new);
        return this;
    }

    public NlpFeaturePipelineFactory<I, O> addFeatureFunctionFactory(List<NlpContextFactory<I, O>> contexts,
                                                                     FeatureExtractor<O, String> extractor,
                                                                     boolean optional) {
        factories.add(new SingleFeatureFunctionFactory(contexts, Collections.singletonList(extractor), optional));
        return this;
    }

    public NlpFeaturePipelineFactory<I, O> addMultiFeatureFunctionFactory(List<NlpContextFactory<I, O>> contexts,
                                                                          FeatureExtractor<O, List<String>> extractor,
                                                                          boolean optional) {
        factories.add(new ListFeatureFunctionFactory(contexts, Collections.singletonList(extractor), optional));
        return this;
    }

    @AllArgsConstructor
    public class SingleFeatureFunctionFactory implements FeatureFunctionFactory<I> {

        private List<NlpContextFactory<I, O>> contexts;
        private List<FeatureExtractor<O, String>> extractors;
        private boolean optional;

        @Override
        public FeatureFunction<I> create() {
            if (optional) {
                if (random.nextInt(contexts.size() + 1) == 0) {
                    return null;
                }
            }
            NlpContextFactory<I, O> contextFactory = contexts.get(random.nextInt(contexts.size()));
            FeatureExtractor<O, String> extractor = extractors.get(random.nextInt(extractors.size()));
            return new StringFeatureFunction<>(contextFactory, Collections.singletonList(extractor));
        }
    }

    @AllArgsConstructor
    public class ListFeatureFunctionFactory implements FeatureFunctionFactory<I> {

        private List<NlpContextFactory<I, O>> contexts;
        private List<FeatureExtractor<O, List<String>>> extractors;
        private boolean optional;

        @Override
        public FeatureFunction<I> create() {
            if (optional) {
                if (random.nextInt(contexts.size() + 1) == 0) {
                    return null;
                }
            }
            NlpContextFactory<I, O> contextFactory = contexts.get(random.nextInt(contexts.size()));
            FeatureExtractor<O, List<String>> extractor = extractors.get(random.nextInt(extractors.size()));
            return new MultiStringFeatureFunction<>(contextFactory, Collections.singletonList(extractor));
        }
    }

}
