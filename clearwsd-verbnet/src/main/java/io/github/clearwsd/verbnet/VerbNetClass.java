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
public interface VerbNetClass {

    /**
     * Return the identifying {@link VerbNetId} for this class.
     */
    VerbNetId verbNetId();

    /**
     * Return a list of the {@link VerbNetMember member verbs} for this class.
     */
    List<VerbNetMember> members();

    /**
     * Return a list of the {@link ThematicRole thematic roles} for this class.
     */
    List<ThematicRole> roles();

    /**
     * Return a list of the {@link VerbNetFrame frames} for this class.
     */
    List<VerbNetFrame> frames();

    /**
     * Return a list of the direct {@link VerbNetClass subclasses} of this class.
     */
    List<VerbNetClass> subclasses();

    /**
     * Optionally return the {@link VerbNetClass parent class} for this class.
     */
    Optional<VerbNetClass> parentClass();

    /**
     * Return all ancestors of this {@link VerbNetClass}.
     *
     * @param includeSelf if True, include this class in the result
     * @return list of all ancestor classes
     */
    default List<VerbNetClass> ancestors(boolean includeSelf) {
        List<VerbNetClass> ancestors = new ArrayList<>();
        if (includeSelf) {
            ancestors.add(this);
        }
        VerbNetClass current = this;
        while (current.parentClass().isPresent()) {
            current = current.parentClass().get();
            ancestors.add(current);
        }
        return ancestors;
    }

    /**
     * Return all ancestors of this {@link VerbNetClass}.
     */
    default List<VerbNetClass> ancestors() {
        return ancestors(false);
    }

    /**
     * Return all descendants of this {@link VerbNetClass}.
     *
     * @param includeSelf if True, include this class in the result
     * @return list of all descendant classes
     */
    default List<VerbNetClass> descendants(boolean includeSelf) {
        List<VerbNetClass> descendants = new ArrayList<>();
        if (includeSelf) {
            descendants.add(this);
        }
        Stack<VerbNetClass> stack = new Stack<>();
        stack.addAll(this.subclasses());
        while (!stack.isEmpty()) {
            VerbNetClass current = stack.pop();
            descendants.add(current);
            stack.addAll(current.subclasses());
        }
        return descendants;
    }

    /**
     * Return all descendants of this {@link VerbNetClass}.
     */
    default List<VerbNetClass> descendants() {
        return descendants(false);
    }

    /**
     * Return all classes related to this {@link VerbNetClass}. In other words, find the root ancestor of this class, then find all
     * descendants.
     */
    default List<VerbNetClass> related() {
        return root().descendants(true);
    }

    /**
     * Returns True if this {@link VerbNetClass} has no ancestors.
     */
    default boolean isRoot() {
        return !parentClass().isPresent();
    }

    /**
     * Returns the root ancestor of this {@link VerbNetClass}, or itself it is already the root.
     */
    default VerbNetClass root() {
        VerbNetClass root = this;
        while (!root.parentClass().isPresent()) {
            root = root.parentClass().get();
        }
        return root;
    }

}
