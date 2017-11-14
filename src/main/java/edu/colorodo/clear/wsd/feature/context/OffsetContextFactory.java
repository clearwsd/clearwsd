package edu.colorodo.clear.wsd.feature.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.type.DepNode;
import edu.colorodo.clear.wsd.type.DependencyTree;
import edu.colorodo.clear.wsd.type.FocusInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Offset-based context factory. Concatenates resulting offsets into a single context.
 *
 * @author jamesgung
 */
@Setter
@Getter
@Accessors(fluent = true)
public class OffsetContextFactory extends DepContextFactory {

    public static final String KEY = "COL";

    private List<Integer> offsets;
    private String id;

    public OffsetContextFactory(List<Integer> offsets) {
        this.offsets = offsets;
        id = String.format("%s[%s]", KEY, offsets.stream()
                .map(o -> Integer.toString(o))
                .collect(Collectors.joining(",")));
    }

    @Override
    public List<NlpContext<DepNode>> apply(FocusInstance<DepNode, DependencyTree> instance) {
        List<DepNode> results = new ArrayList<>();
        for (Integer offset : offsets) {
            int containerIndex = instance.focus().index() + offset;
            if (containerIndex < 0 || containerIndex >= instance.sequence().size()) {
                continue;
            }
            results.add(instance.sequence().get(containerIndex));
        }
        return Collections.singletonList(new NlpContext<>(id, results));
    }
}
