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

import io.github.clearwsd.verbnet.semantics.SemanticPredicate;
import io.github.clearwsd.verbnet.syntax.SyntacticPhrase;
import java.util.List;

/**
 * VerbNet class frame.
 *
 * @author jgung
 */
public interface VerbNetFrame extends FrameDescription {

    /**
     * Provides a human-readable description and identifying information for this {@link VerbNetFrame}.
     */
    FrameDescription description();

    /**
     * Return a list of example sentences for this {@link VerbNetFrame}.
     */
    List<String> examples();

    /**
     * Return the {@link SyntacticPhrase syntactic phrases} for this {@link VerbNetFrame}.
     */
    List<SyntacticPhrase> syntax();

    /**
     * Return the {@link SemanticPredicate semantic predicates} associated with this {@link VerbNetFrame}.
     */
    List<SemanticPredicate> predicates();

    /**
     * Return the VerbNet class for this {@link VerbNetFrame}.
     */
    VerbNetClass verbClass();

}
