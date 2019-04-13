package io.github.clearwsd.verbnet;

import io.github.clearwsd.verbnet.xml.WordNetKey;
import java.util.List;

/**
 * Individual VerbNet class member.
 *
 * @author jgung
 */
public interface VerbNetMember {

    String name();

    List<WordNetKey> wn();

    String features();

    String grouping();

    String verbnetKey();

    VerbNetClass verbClass();

}
