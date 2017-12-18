package edu.colorado.clear.wsd.feature.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.type.NlpSequence;

/**
 * Utilities for commonly used NLP contexts.
 *
 * @author jamesgung
 */
public class Contexts {

    private Contexts() {
    }

    public static DepChildrenContextFactory excludingDeps(Set<String> exclusions) {
        return new DepChildrenContextFactory(exclusions, new HashSet<>());
    }

    public static DepChildrenContextFactory includingDeps(Set<String> inclusions) {
        return new DepChildrenContextFactory(inclusions);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> focus() {
        return new OffsetContextFactory<>(0);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> window(Integer... offsets) {
        return new OffsetContextFactory<>(offsets);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> window(Collection<Integer> offsets) {
        return new OffsetContextFactory<>(new ArrayList<>(offsets));
    }

    public static RootPathContextFactory head() {
        return new RootPathContextFactory(false, 1);
    }

}
