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
import java.util.Collections;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.type.NlpSequence;

/**
 * Context factory that returns the whole sequence of input tokens as a single context.
 *
 * @author jamesgung
 */
public class SequenceIdentifyContextFactory<S extends NlpSequence<T>, T extends NlpInstance>
        implements NlpContextFactory<S, T> {

    private static final long serialVersionUID = -6999855605864180292L;

    private static final String KEY = "I";

    @Override
    public List<NlpContext<T>> apply(S instance) {
        return Collections.singletonList(new NlpContext<>(KEY, new ArrayList<>(instance.tokens())));
    }

}
