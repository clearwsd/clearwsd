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

/**
 * Tokenizer/sentence segmenter for natural language text. Splits text into sentences with {@link NlpTokenizer#segment(String)},
 * and further splits an individual sentence into a list of tokens through {@link NlpTokenizer#tokenize(String)}.
 *
 * @author jamesgung
 */
public interface NlpTokenizer {

    /**
     * Split raw input text into a list of sentences. Each {@link String} returned in the list should be a single sentence.
     *
     * @param input input text
     * @return list of sentence strings
     */
    List<String> segment(String input);

    /**
     * Split an input sentence into individual tokens.
     *
     * @param sentence single sentence
     * @return list of tokens
     */
    List<String> tokenize(String sentence);

}
