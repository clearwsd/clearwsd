package io.github.clearwsd.verbnet;

import io.github.clearwsd.verbnet.xml.WordNetKey;
import java.util.List;
import java.util.Set;

/**
 * VerbNet index providing methods for retrieving VerbNet classes and members.
 *
 * @author jamesgung
 */
public interface VerbIndex {

    List<VerbNetClass> roots();

    VerbNetClass getById(String id);

    Set<VerbNetClass> getByLemma(String lemma);

    Set<VerbNetMember> getMembersByLemma(String lemma);

    Set<VerbNetMember> getMembersByWordNetKey(WordNetKey wordNetKey);

    Set<WordNetKey> getWordNetKeysByLemma(String lemma);

}
