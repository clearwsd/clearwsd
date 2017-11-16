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
 * Offset-based context factory. Provides option to concatenate resulting offsets into a single context,
 * or create a separate context for each offset.
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
    private boolean concatenate = false;

    public OffsetContextFactory(List<Integer> offsets) {
        this.offsets = offsets;
        id = String.format("%s[%s]", KEY, offsets.stream()
                .map(o -> Integer.toString(o))
                .collect(Collectors.joining(",")));
    }

    @Override
    public List<NlpContext<DepNode>> apply(FocusInstance<DepNode, DependencyTree> instance) {
        if (concatenate) {
            return applyConcatenate(instance);
        }
        return applySeparate(instance);
    }

    private List<NlpContext<DepNode>> applySeparate(FocusInstance<DepNode, DependencyTree> instance) {
        List<NlpContext<DepNode>> results = new ArrayList<>();
        for (Integer offset : offsets) {
            int containerIndex = instance.focus().index() + offset;
            if (containerIndex < 0 || containerIndex >= instance.sequence().size()) {
                continue;
            }
            results.add(new NlpContext<>(String.format("%s[%s]", KEY, offset), instance.sequence().get(containerIndex)));
        }
        return results;
    }

    private List<NlpContext<DepNode>> applyConcatenate(FocusInstance<DepNode, DependencyTree> instance) {

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
