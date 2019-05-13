/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.feature.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;

/**
 * Utilities for commonly used NLP contexts.
 *
 * @author jamesgung
 */
public class Contexts {

    private Contexts() {
    }

    public static DepChildPathContextFactory depPath(Map<String, List<List<String>>> exclusions) {
        return new DepChildPathContextFactory(exclusions);
    }

    public static DepChildrenContextFactory excludingDeps(Set<String> exclusions) {
        return new DepChildrenContextFactory(exclusions, new HashSet<>());
    }

    public static DepChildrenContextFactory excludingDeps(String... exclusions) {
        return new DepChildrenContextFactory(Arrays.stream(exclusions).collect(Collectors.toSet()), new HashSet<>());
    }

    public static DepChildrenContextFactory includingDeps(Set<String> inclusions) {
        return new DepChildrenContextFactory(inclusions);
    }

    public static DepChildrenContextFactory includingDeps(Set<String> inclusions, int level) {
        return new DepChildrenContextFactory(Collections.emptySet(), inclusions, level);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> focus() {
        return new OffsetContextFactory<>(0);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> window(Integer... offsets) {
        return new OffsetContextFactory<>(offsets);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> window(boolean concatenate,
                                                                                                      Integer... offsets) {
        return new OffsetContextFactory<>(concatenate, offsets);
    }

    public static <T extends NlpInstance, S extends NlpSequence<T>> OffsetContextFactory<T, S> window(Collection<Integer> offsets) {
        return new OffsetContextFactory<>(new ArrayList<>(offsets));
    }

    public static RootPathContextFactory head() {
        return new RootPathContextFactory(false, 1);
    }

}
