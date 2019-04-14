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

package io.github.clearwsd.verbnet.syntax;

import io.github.clearwsd.verbnet.restrictions.Restrictions;
import java.util.List;

/**
 * Nominal syntactic phrase.
 *
 * @author jamesgung
 */
public interface NounPhrase extends SyntacticPhrase {

    /**
     * Thematic role associated with this NP.
     */
    String thematicRole();

    /**
     * Syntactic restrictions associated with this NP.
     */
    List<Restrictions<String>> syntacticRestrictions();

    /**
     * Additional selectional restrictions (beyond those at the thematic role level) for this NP.
     */
    List<Restrictions<String>> selectionalRestrictions();

}
