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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.type.NlpInstance;
import lombok.AllArgsConstructor;

/**
 * Composite context factory.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class CompositeContextFactory<InputT extends NlpInstance, OutputT extends NlpInstance>
        implements NlpContextFactory<InputT, OutputT> {

    private static final long serialVersionUID = -7801892086796933208L;

    private List<NlpContextFactory<InputT, OutputT>> contextFactories;

    @SafeVarargs
    public CompositeContextFactory(NlpContextFactory<InputT, OutputT>... contextFactories) {
        this.contextFactories = Arrays.asList(contextFactories);
    }

    @Override
    public List<NlpContext<OutputT>> apply(InputT instance) {
        return contextFactories.stream()
                .map(c -> c.apply(instance))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
