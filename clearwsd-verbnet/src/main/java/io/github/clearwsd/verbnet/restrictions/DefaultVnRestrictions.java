/*
 * Copyright 2019 James Gung
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

package io.github.clearwsd.verbnet.restrictions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * {@link VnRestrictions} default implementation.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class DefaultVnRestrictions<T> implements VnRestrictions<T> {

    private Set<T> include = new HashSet<>();

    private Set<T> exclude = new HashSet<>();

    @SafeVarargs
    public static <S> DefaultVnRestrictions<S> excluding(S... excluding) {
        DefaultVnRestrictions<S> res = new DefaultVnRestrictions<>();
        res.exclude.addAll(Arrays.asList(excluding));
        return res;
    }

    @SafeVarargs
    public static <S> DefaultVnRestrictions<S> including(S... including) {
        DefaultVnRestrictions<S> res = new DefaultVnRestrictions<>();
        res.include.addAll(Arrays.asList(including));
        return res;
    }

    public static <S> DefaultVnRestrictions<S> includingExcluding(@NonNull Collection<S> including,
        @NonNull Collection<S> excluding) {
        DefaultVnRestrictions<S> res = new DefaultVnRestrictions<>();
        res.include.addAll(including);
        res.exclude.addAll(excluding);
        return res;
    }

    public static <S> DefaultVnRestrictions<S> map(@NonNull VnRestrictions<String> restrictions,
        @NonNull Function<String, S> mapper) {
        DefaultVnRestrictions<S> result = new DefaultVnRestrictions<>();
        result.include(restrictions.include().stream().map(mapper).collect(Collectors.toSet()));
        result.exclude(restrictions.exclude().stream().map(mapper).collect(Collectors.toSet()));
        return result;
    }

    public static <S> List<DefaultVnRestrictions<S>> map(@NonNull List<VnRestrictions<String>> restrictions,
        @NonNull Function<String, S> mapper) {
        return restrictions.stream().map(res -> map(res, mapper)).collect(Collectors.toList());
    }
}
