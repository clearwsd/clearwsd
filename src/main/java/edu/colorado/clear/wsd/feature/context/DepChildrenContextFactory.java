package edu.colorado.clear.wsd.feature.context;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.type.DependencyTree;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Context factory returning the list of children of a particular dependency node.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class DepChildrenContextFactory extends DepContextFactory {

    public static final String KEY = "CHILD";

    @JsonProperty
    private Set<String> exclude;
    @JsonProperty
    private Set<String> include;

    /**
     * Initialize a {@link DepChildrenContextFactory} with child dependency label exclusions and inclusions.
     *
     * @param exclude labels to exclude (if empty, exclude none)
     * @param include labels to include (if empty, include all)
     */
    public DepChildrenContextFactory(Set<String> exclude, Set<String> include) {
        this.exclude = exclude;
        this.include = include;
    }

    public DepChildrenContextFactory() {
        this(new HashSet<>(), new HashSet<>());
    }

    @Override
    public List<NlpContext<DepNode>> apply(FocusInstance<DepNode, DependencyTree> instance) {
        return instance.focus().children().stream()
                .filter(c -> include.size() == 0 || include.contains(c.dep()))
                .filter(c -> exclude.size() == 0 || !exclude.contains(c.dep()))
                .map(c -> new NlpContext<>(KEY, c))
                .collect(Collectors.toList());
    }

}
