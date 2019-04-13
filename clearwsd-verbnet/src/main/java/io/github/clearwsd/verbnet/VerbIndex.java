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

    List<VerbNetClass> roots();

    VerbNetClass getById(String id);

    Set<VerbNetClass> getByLemma(String lemma);

    Set<VerbNetMember> getMembersByLemma(String lemma);

    Set<VerbNetMember> getMembersByWordNetKey(WordNetKey wordNetKey);

    Set<WordNetKey> getWordNetKeysByLemma(String lemma);

}
