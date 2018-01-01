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

import java.util.Arrays;
import java.util.List;

import io.github.clearwsd.parser.NlpTokenizer;

/**
 * Whitespace tokenizer implementation used mostly for pre-tokenized/space-separated/formatted text, not intended for actual use in
 * a natural language setting. Tokens are split on whitespace, and sentences are split on newlines.
 *
 * @author jamesgung
 */
public class WhitespaceTokenizer implements NlpTokenizer {

    @Override
    public List<String> segment(String input) {
        return Arrays.asList(input.split("\\n"));
    }

    @Override
    public List<String> tokenize(String sentence) {
        return Arrays.asList(sentence.split("\\s+"));
    }
}
