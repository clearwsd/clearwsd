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

import java.util.Optional;
import java.util.Set;

/**
 * WordNet facade to allow alternative WN implementations to be used during feature extraction.
 *
 * @author jamesgung
 */
public interface WordNetFacade {

    /**
     * Return the most frequent sense for a given lemma and part-of-speech tag.
     *
     * @param lemma lemma
     * @param pos   part-of-speech tag
     * @return most frequent sense sense key
     */
    Optional<String> mfs(String lemma, String pos);

    /**
     * Return all senses for a given lemma/POS combination.
     *
     * @param lemma lemma
     * @param pos   part-of-speech tag
     * @return all sense keys for input lemma/POS
     */
    Set<String> senses(String lemma, String pos);

    /**
     * Return hypernyms for a given lemma and part-of-speech tag (words in synsets that are hypernyms of synsets to which the given
     * lemma/POS combination belongs).
     *
     * @param lemma lemma
     * @param pos   part-of-speech tag
     * @return synonyms
     */
    Set<String> hypernyms(String lemma, String pos);

    /**
     * Return synonyms for a given lemma and part-of-speech tag (words belonging to synsets for a particular lemma/POS combination).
     *
     * @param lemma lemma
     * @param pos   part-of-speech tag
     * @return synonyms
     */
    Set<String> synonyms(String lemma, String pos);

}
