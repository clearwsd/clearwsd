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
import io.github.clearwsd.verbnet.syntax.VnSyntax;
import java.util.List;

/**
 * VerbNet class frame.
 *
 * @author jgung
 */
public interface VnFrame extends VnFrameDescription {

    /**
     * Provides a human-readable description and identifying information for this {@link VnFrame}.
     */
    VnFrameDescription description();

    /**
     * Return a list of example sentences for this {@link VnFrame}.
     */
    List<String> examples();

    /**
     * Return the {@link VnSyntax syntactic phrases} for this {@link VnFrame}.
     */
    List<VnSyntax> syntax();

    /**
     * Return the {@link VnSemanticPredicate semantic predicates} associated with this {@link VnFrame}.
     */
    List<VnSemanticPredicate> predicates();

    /**
     * Return the VerbNet class for this {@link VnFrame}.
     */
    VnClass verbClass();

}
