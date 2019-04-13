/*
 * Copyright 2019 James Gung
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

package io.github.clearwsd.verbnet;

import io.github.clearwsd.verbnet.xml.WordNetKey;
import java.util.List;
import java.util.Set;

/**
 * VerbNet index providing methods for retrieving VerbNet classes and members.
 *
 * @author jamesgung
 */
public interface VerbIndex {

    /**
     * Return a list of all root {@link VerbNetClass classes} in this {@link VerbIndex}.
     */
    List<VerbNetClass> roots();

    /**
     * Returns the VerbNet class for a given VerbNet class ID. Supported ID formats include "begin-55.1-1" or "55.1-1".
     */
    VerbNetClass getById(String id);

    /**
     * Return a set of {@link VerbNetClass classes} corresponding to a given lemma.
     */
    Set<VerbNetClass> getByLemma(String lemma);

    /**
     * Return the set of {@link VerbNetClass class} {@link VerbNetMember members} for a given lemma.
     */
    Set<VerbNetMember> getMembersByLemma(String lemma);

    /**
     * Return the set of {@link VerbNetClass class} {@link VerbNetMember members} for a given {@link WordNetKey}.
     */
    Set<VerbNetMember> getMembersByWordNetKey(WordNetKey wordNetKey);

    /**
     * Return the set of {@link WordNetKey WordNet keys/senses} for a given lemma.
     */
    Set<WordNetKey> getWordNetKeysByLemma(String lemma);

}
