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

package io.github.clearwsd.type;

import java.util.List;

/**
 * List of tokens ({@link NlpInstance NlpInstances}), such as a sentence, dependency tree, or document.
 *
 * @param <T> token type
 * @author jamesgung
 */
public interface NlpSequence<T extends NlpInstance> extends NlpInstance, Iterable<T> {

    /**
     * List of tokens in this NLP sequence.
     */
    List<T> tokens();

    /**
     * Get the token at a specified index.
     *
     * @param index token index
     * @return token at index
     */
    T get(int index);

    /**
     * Number of tokens in this instance.
     */
    int size();

}
