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

package io.github.clearwsd.verbnet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * {@link Restrictions} default implementation.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class DefaultRestrictions<T> implements Restrictions<T> {

    private Set<T> include = new HashSet<>();

    private Set<T> exclude = new HashSet<>();

    @SafeVarargs
    public static <S> DefaultRestrictions<S> excluding(S... excluding) {
        DefaultRestrictions<S> res = new DefaultRestrictions<>();
        res.exclude.addAll(Arrays.asList(excluding));
        return res;
    }

    @SafeVarargs
    public static <S> DefaultRestrictions<S> including(S... including) {
        DefaultRestrictions<S> res = new DefaultRestrictions<>();
        res.include.addAll(Arrays.asList(including));
        return res;
    }

    public static <S> DefaultRestrictions<S> includingExcluding(Collection<S> including, Collection<S> excluding) {
        DefaultRestrictions<S> res = new DefaultRestrictions<>();
        res.include.addAll(including);
        res.exclude.addAll(excluding);
        return res;
    }
}
