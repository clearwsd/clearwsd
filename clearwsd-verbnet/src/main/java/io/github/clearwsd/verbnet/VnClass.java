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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * Individual VerbNet class.
 *
 * @author jgung
 */
public interface VnClass {

    /**
     * Return the identifying {@link VnClassId} for this class.
     */
    VnClassId verbNetId();

    /**
     * Return a list of the {@link VnMember member verbs} for this class.
     */
    List<VnMember> members();

    /**
     * Return a list of the {@link VnThematicRole thematic roles} for this class.
     */
    List<VnThematicRole> roles();

    /**
     * Return a list of the {@link VnFrame frames} for this class.
     */
    List<VnFrame> frames();

    /**
     * Return a list of the direct {@link VnClass subclasses} of this class.
     */
    List<VnClass> subclasses();

    /**
     * Optionally return the {@link VnClass parent class} for this class.
     */
    Optional<VnClass> parentClass();

    /**
     * Return all ancestors of this {@link VnClass}.
     *
     * @param includeSelf if True, include this class in the result
     * @return list of all ancestor classes
     */
    default List<VnClass> ancestors(boolean includeSelf) {
        List<VnClass> ancestors = new ArrayList<>();
        if (includeSelf) {
            ancestors.add(this);
        }
        VnClass current = this;
        while (current.parentClass().isPresent()) {
            current = current.parentClass().get();
            ancestors.add(current);
        }
        return ancestors;
    }

    /**
     * Return all ancestors of this {@link VnClass}.
     */
    default List<VnClass> ancestors() {
        return ancestors(false);
    }

    /**
     * Return all descendants of this {@link VnClass}.
     *
     * @param includeSelf if True, include this class in the result
     * @return list of all descendant classes
     */
    default List<VnClass> descendants(boolean includeSelf) {
        List<VnClass> descendants = new ArrayList<>();
        if (includeSelf) {
            descendants.add(this);
        }
        Stack<VnClass> stack = new Stack<>();
        stack.addAll(this.subclasses());
        while (!stack.isEmpty()) {
            VnClass current = stack.pop();
            descendants.add(current);
            stack.addAll(current.subclasses());
        }
        return descendants;
    }

    /**
     * Return all descendants of this {@link VnClass}.
     */
    default List<VnClass> descendants() {
        return descendants(false);
    }

    /**
     * Return all classes related to this {@link VnClass}. In other words, find the root ancestor of this class, then find all
     * descendants.
     */
    default List<VnClass> related() {
        return root().descendants(true);
    }

    /**
     * Returns True if this {@link VnClass} has no ancestors.
     */
    default boolean isRoot() {
        return !parentClass().isPresent();
    }

    /**
     * Returns the root ancestor of this {@link VnClass}, or itself it is already the root.
     */
    default VnClass root() {
        VnClass root = this;
        while (root.parentClass().isPresent()) {
            root = root.parentClass().get();
        }
        return root;
    }

}
