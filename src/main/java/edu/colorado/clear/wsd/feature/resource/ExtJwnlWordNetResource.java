package edu.colorado.clear.wsd.feature.resource;

import com.google.common.base.Stopwatch;

import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.feature.util.PosUtils;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


/**
 * WordNet resource wrapping ExtJWNL (<a href="https://github.com/extjwnl/extjwnl">https://github.com/extjwnl/extjwnl</a>).
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class ExtJwnlWordNetResource<K extends NlpInstance> implements FeatureResource<K, List<String>> {

    private static final long serialVersionUID = 4520884471486094705L;

    public static final String KEY = "WN";

    @Getter
    private String key = KEY;

    private Dictionary dict;

    public ExtJwnlWordNetResource(String inputPath) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (inputPath != null) {
                dict = Dictionary.getFileBackedInstance(inputPath);
            } else {
                dict = Dictionary.getDefaultResourceInstance();
            }
            log.debug("Loaded WordNet in {}.", stopwatch.toString());
        } catch (Exception e) {
            log.error("Error loading WordNet dictionary.", e);
        }
    }

    public ExtJwnlWordNetResource() {
        this(null);
    }

    @Override
    public List<String> lookup(K key) {
        return new ArrayList<>(hypernyms(key.feature(FeatureType.Lemma), key.feature(FeatureType.Pos)));
    }

    private Set<String> hypernyms(String lemma, String pos) {
        Set<String> words = new HashSet<>();
        try {
            POS wnPos = getPos(pos);
            if (!POS.NOUN.equals(wnPos)) {
                return words;
            }
            IndexWord indexWord = dict.getIndexWord(wnPos, lemma);
            if (indexWord == null) {
                return words;
            }
            for (Synset id : indexWord.getSenses()) {
                words.addAll(words(id));
                words.addAll(PointerUtils.getDirectHypernyms(id).stream()
                        .map(node -> words(node.getSynset()))
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()));
            }
        } catch (Exception e) {
            log.warn("Error getting WordNet hypernyms for {}-{}", lemma, pos, e);
        }
        return words;
    }

    private static Set<String> words(Synset synset) {
        return synset.getWords().stream()
                .map(Word::getLemma)
                .collect(Collectors.toSet());
    }

    private static POS getPos(String pos) {
        if (PosUtils.isNoun(pos)) {
            return POS.NOUN;
        } else if (PosUtils.isVerb(pos)) {
            return POS.VERB;
        } else if (PosUtils.isAdjective(pos)) {
            return POS.ADJECTIVE;
        } else if (PosUtils.isAdverb(pos)) {
            return POS.ADVERB;
        } else {
            return null;
        }
    }

}
