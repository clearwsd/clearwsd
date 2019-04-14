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

import io.github.clearwsd.verbnet.semantics.VnSemanticPredicate;
import java.util.List;

/**
 * Individual VerbNet class member verb.
 *
 * @author jgung
 */
public interface VnMember {

    /**
     * Return the name of this member, e.g. "run".
     */
    String name();

    /**
     * Return a list of {@link WnKey WordNet synsets} this member is mapped to.
     */
    List<WnKey> wn();

    /**
     * Returns any member-specific features applicable in {@link VnSemanticPredicate semantic predicates} for the VerbNet class,
     * e.g. "increase".
     */
    List<String> features();

    /**
     * Returns the OntoNotes groupings sense for this {@link VnMember}, e.g. "sever.01 sever.02".
     */
    List<String> groupings();

    /**
     * Returns the VerbNet key for this member, e.g. "sever#2".
     */
    String verbnetKey();

    /**
     * Returns the {@link VnClass} this member belongs to.
     */
    VnClass verbClass();

}
