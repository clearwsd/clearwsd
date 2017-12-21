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
