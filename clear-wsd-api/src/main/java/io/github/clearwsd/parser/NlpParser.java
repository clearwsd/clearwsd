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

package io.github.clearwsd.parser;

import java.util.List;

import io.github.clearwsd.type.DepTree;

/**
 * Syntactic dependency parser, used to produce {@link DepTree DepTrees} from raw text. Extends from {@link NlpTokenizer} to ensure
 * that any pre-tokenized/segmented text can be handled, while still handling raw un-tokenized text, such as from a document.
 *
 * @author jamesgung
 */
public interface NlpParser extends NlpTokenizer {

    /**
     * Parse a single tokenized sentence, producing an {@link DepTree}.
     *
     * @param tokens tokenized text of a single sentence
     * @return syntactic dependency tree
     */
    DepTree parse(List<String> tokens);

}
