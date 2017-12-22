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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Context factory that filters out sub-contexts.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor
public class FilteringContextFactory<OutputT extends NlpInstance> implements NlpContextFactory<List<NlpContext<OutputT>>, OutputT> {

    private static final long serialVersionUID = 1281478146530198871L;

    private String key;
    private Set<String> include;
    private Set<String> exclude;

    public FilteringContextFactory(String key, Set<String> include) {
        this(key, include, new HashSet<>());
    }

    @Override
    public List<NlpContext<OutputT>> apply(List<NlpContext<OutputT>> contexts) {
        return contexts.stream()
                .map(context -> {
                    //noinspection SuspiciousMethodCalls
                    List<OutputT> results = context.tokens().stream()
                            .filter(c -> include.size() == 0 || include.contains(c.feature(key)))
                            .filter(c -> exclude.size() == 0 || !exclude.contains(c.feature(key)))
                            .collect(Collectors.toList());
                    return new NlpContext<>(context.identifier(), results);
                })
                .filter(c -> c.tokens().size() > 0)
                .collect(Collectors.toList());
    }

}
