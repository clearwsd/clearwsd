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

import java.util.List;

/**
 * VerbNet semantic predicate, providing underlying components of meaning for an event, its participants and sub-events.
 *
 * @author jgung
 */
public interface SemanticPredicate {

    /**
     * Truth value for this semantic predicate, e.g. "!".
     */
    String bool();

    /**
     * Value of this semantic predicate, e.g. "has_possession".
     */
    String value();

    /**
     * Collection of arguments of this semantic predicate.
     */
    List<SemanticArgument> semanticArguments();

}
