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

    VerbNetId verbNetId();

    List<VerbNetMember> members();

    List<ThematicRole> roles();

    List<VerbNetFrame> frames();

    List<VerbNetClass> subclasses();

    Optional<VerbNetClass> parentClass();

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

    default List<VerbNetClass> ancestors() {
        return ancestors(false);
    }

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

    default List<VerbNetClass> descendants() {
        return descendants(false);
    }

    default List<VerbNetClass> related() {
        return root().descendants(true);
    }

    default boolean isRoot() {
        return !parentClass().isPresent();
    }

    default VerbNetClass root() {
        VerbNetClass root = this;
        while (!root.parentClass().isPresent()) {
            root = root.parentClass().get();
        }
        return root;
    }

}
