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

package io.github.clearwsd.utils;

import java.util.Set;

/**
 * Sense inventory mapping word lemmas onto lists of senses.
 *
 * @author jamesgung
 */
public interface SenseInventory<T> {

    String DEFAULT_SENSE = "NONE";

    /**
     * Return all labels associated with a particular word form.
     *
     * @param lemma word base form
     * @return set of labels/senses associated with the input word
     */
    Set<String> senses(String lemma);

    /**
     * Returns the most frequent sense/default sense associated with a particular input.
     *
     * @param lemma word base form
     * @return default sense for input word
     */
    String defaultSense(String lemma);

    /**
     * Add a sense to the sense inventory.
     *
     * @param lemma base word
     * @param sense sense of word
     */
    void addSense(String lemma, String sense);

    /**
     * Return the sense object associated with this sense inventory.
     *
     * @param id sense unique id
     * @return sense
     */
    T getSense(String id);

}
