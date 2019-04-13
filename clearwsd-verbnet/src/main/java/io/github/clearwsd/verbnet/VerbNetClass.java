package io.github.clearwsd.verbnet;

import java.util.List;
import java.util.Optional;

/**
 * Individual VerbNet class.
 *
 * @author jgung
 */
public interface VerbNetClass {

    String id();

    List<VerbNetMember> members();

    List<ThematicRole> roles();

    List<VerbNetFrame> frames();

    List<VerbNetClass> subclasses();

    Optional<VerbNetClass> parentClass();

}
