package edu.colorodo.clear.wsd.feature.context;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.type.DepNode;
import edu.colorodo.clear.wsd.type.DependencyTree;
import edu.colorodo.clear.wsd.type.FocusInstance;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Offset-based context factory. Provides option to either concatenate resulting offsets into a single context,
 * or create a separate context for each offset.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class OffsetContextFactory extends DepContextFactory {

    public static final String KEY = "COL";

    @JsonProperty
    private List<Integer> offsets;
    @JsonProperty
    private boolean concatenate;

    private String id;

    public OffsetContextFactory(@JsonProperty("offsets") List<Integer> offsets,
                                @JsonProperty("concatenate") boolean concatenate) {
        this.offsets = offsets;
        this.concatenate = concatenate;
        id = String.format("%s[%s]", KEY, offsets.stream()
                .map(o -> Integer.toString(o))
                .collect(Collectors.joining(",")));
    }

    public OffsetContextFactory(List<Integer> offsets) {
        this(offsets, false);
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
