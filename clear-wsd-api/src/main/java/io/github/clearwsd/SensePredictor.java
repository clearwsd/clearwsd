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

package io.github.clearwsd;

import java.util.List;

/**
 * Minimal prediction interface for word senses. For most NLP applications, tokenization will already be present. Providing a
 * pre-tokenized sentence ensures the consistency of tokenization with the internal processing performed by the WSD system
 * and should avoid headaches involved in "realigning" the predicted senses with the original input text.
 *
 * @param <S> sense type
 * @author jamesgung
 */
@FunctionalInterface
public interface SensePredictor<S> {

    /**
     * Given a tokenized sentence, return a list of senses (one per input token).
     *
     * @param sentence tokenized sentence
     * @return list of senses (one per input token)
     */
    List<S> predict(List<String> sentence);

}
