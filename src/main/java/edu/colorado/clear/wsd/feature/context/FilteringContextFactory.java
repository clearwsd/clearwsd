package edu.colorado.clear.wsd.feature.context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.Setter;

/**
 * Context factory that filters out sub-contexts.
 *
 * @author jamesgung
 */
@Getter
@Setter
public class FilteringContextFactory<OutputT extends NlpInstance> implements NlpContextFactory<List<NlpContext<OutputT>>, OutputT> {

    private static final long serialVersionUID = 1281478146530198871L;

    private String key;
    private Set<String> include;
    private Set<String> exclude;

    public FilteringContextFactory(String key, Set<String> include, Set<String> exclude) {
        this.key = key;
        this.include = include;
        this.exclude = exclude;
    }

    public FilteringContextFactory(String key, Set<String> include) {
        this(key, include, new HashSet<>());
    }

    @Override
    public List<NlpContext<OutputT>> apply(List<NlpContext<OutputT>> contexts) {
        return contexts.stream().map(context -> {
            //noinspection SuspiciousMethodCalls
            List<OutputT> results = context.tokens().stream()
                    .filter(c -> include.size() == 0 || include.contains(c.feature(key)))
                    .filter(c -> exclude.size() == 0 || !exclude.contains(c.feature(key)))
                    .collect(Collectors.toList());
            return new NlpContext<>(context.identifier(), results);
        }).filter(c -> c.tokens().size() > 0)
                .collect(Collectors.toList());
    }

}
