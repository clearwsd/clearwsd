package io.github.clearwsd.feature.context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import lombok.AllArgsConstructor;

/**
 * Composite context factory.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class CompositeContextFactory<InputT extends NlpInstance, OutputT extends NlpInstance>
        implements NlpContextFactory<InputT, OutputT> {

    private static final long serialVersionUID = -7801892086796933208L;

    private List<NlpContextFactory<InputT, OutputT>> contextFactories;

    @SafeVarargs
    public CompositeContextFactory(NlpContextFactory<InputT, OutputT>... contextFactories) {
        this.contextFactories = Arrays.asList(contextFactories);
    }

    @Override
    public List<NlpContext<OutputT>> apply(InputT instance) {
        return contextFactories.stream()
                .map(c -> c.apply(instance))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
