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

package io.github.clearwsd.type;

import java.util.List;

/**
 * Node in a dependency tree representing a single word with multiple syntactic children ({@link DepNode#children()}) and a single
 * syntactic head ({@link DepNode#head()}.
 *
 * @author jamesgung
 */
public interface DepNode extends NlpInstance {

    /**
     * True if this dependency node is the root of a dependency tree, indicating that its head is nonexistent (null).
     */
    boolean isRoot();

    /**
     * Dependency relation (to head) associated with this node.
     */
    String dep();

    /**
     * Syntactic head of this dependency node, null when this node is the root of the tree.
     */
    DepNode head();

    /**
     * Syntactic children of this dependency node.
     */
    List<DepNode> children();

}
