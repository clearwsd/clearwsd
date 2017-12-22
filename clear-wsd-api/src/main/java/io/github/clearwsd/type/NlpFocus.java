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

/**
 * NLP focus/sequence pair, such as for classification of a word ({@link NlpInstance}) within a sentence ({@link NlpSequence} of
 * words). Used mostly to organize relevant information for feature extraction where overall features of the container sequence
 * are important, in addition to information about the position of the target within the sequence. Especially useful for
 * token-level classification, such as in sense disambiguation.
 *
 * @param <T> focus type
 * @param <S> sequence type
 * @author jamesgung
 */
public interface NlpFocus<T extends NlpInstance, S extends NlpSequence<T>> extends NlpSequence<T> {

    /**
     * Return the focus within the sequence.
     */
    T focus();

    /**
     * Return the overall sequence containing the focus.
     */
    S sequence();

}
