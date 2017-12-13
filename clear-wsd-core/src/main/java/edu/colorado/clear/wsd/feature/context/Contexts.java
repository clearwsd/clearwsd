package edu.colorado.clear.wsd.feature.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

    public static OffsetContextFactory focus() {
        return new OffsetContextFactory(0);
    }

    public static OffsetContextFactory window(Integer... offsets) {
        return new OffsetContextFactory(offsets);
    }

    public static OffsetContextFactory window(Collection<Integer> offsets) {
        return new OffsetContextFactory(new ArrayList<>(offsets));
    }

    public static RootPathContextFactory head() {
        return new RootPathContextFactory(false, 1);
    }

}
