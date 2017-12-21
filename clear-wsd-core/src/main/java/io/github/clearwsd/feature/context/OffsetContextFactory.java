package io.github.clearwsd.feature.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;

/**
 * Offset-based context factory. Provides option to either concatenate resulting offsets into a single context,
 * or create a separate context for each offset.
 *
 * @author jamesgung
 */
public class OffsetContextFactory<T extends NlpInstance, S extends NlpSequence<T>> implements NlpContextFactory<NlpFocus<T, S>, T> {

    public static final String KEY = "COL";

    private static final long serialVersionUID = 1961214534857020518L;

    private List<Integer> offsets;
    private boolean concatenate;
    private String id;

    public OffsetContextFactory(List<Integer> offsets, boolean concatenate) {
        this.offsets = offsets;
        this.concatenate = concatenate;
        id = String.format("%s[%s]", KEY, offsets.stream()
                .map(o -> Integer.toString(o))
                .collect(Collectors.joining(",")));
    }

    public OffsetContextFactory(List<Integer> offsets) {
        this(offsets, false);
    }

    public OffsetContextFactory(Integer... offsets) {
        this(Arrays.asList(offsets), false);
    }

    public OffsetContextFactory(boolean concatenate, Integer... offsets) {
        this(Arrays.asList(offsets), concatenate);
    }

    @Override
    public List<NlpContext<T>> apply(NlpFocus<T, S> instance) {
        if (concatenate) {
            return applyConcatenate(instance);
        }
        return applySeparate(instance);
    }

    private List<NlpContext<T>> applySeparate(NlpFocus<T, S> instance) {
        List<NlpContext<T>> results = new ArrayList<>();
        for (Integer offset : offsets) {
            int containerIndex = instance.focus().index() + offset;
            if (containerIndex < 0 || containerIndex >= instance.sequence().size()) {
                continue;
            }
            results.add(new NlpContext<>(String.format("%s[%s]", KEY, offset), instance.sequence().get(containerIndex)));
        }
        return results;
    }

    private List<NlpContext<T>> applyConcatenate(NlpFocus<T, S> instance) {
        List<T> results = new ArrayList<>();
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
