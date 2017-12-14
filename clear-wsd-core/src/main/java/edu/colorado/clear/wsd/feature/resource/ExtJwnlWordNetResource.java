package edu.colorado.clear.wsd.feature.resource;

import com.google.common.base.Stopwatch;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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

    public static final String WN_KEY = "WN";

    @Getter
    private String key = WN_KEY;

    @Getter
    private Dictionary dict;

    public ExtJwnlWordNetResource(String inputPath) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            dict = inputPath == null ? Dictionary.getDefaultResourceInstance() : Dictionary.getFileBackedInstance(inputPath);
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

    /**
     * Return the most frequent sense of a word/POS combination based on tagged sense counts.
     *
     * @param lemma word lemma
     * @param pos   word part-of-speech
     * @return most frequent sense
     */
    @Nullable
    public String mostFrequentSense(String lemma, String pos) {
        POS wnPos = getPos(pos);
        try {
            IndexWord indexWord = dict.lookupIndexWord(wnPos, lemma);
            if (indexWord == null || indexWord.getSenses().size() == 0) {
                return null;
            }
            Synset synset = indexWord.getSenses().get(0);
            int index = synset.indexOfWord(lemma.replaceAll("_", " "));
            return synset.getWords().get(index).getSenseKey();
        } catch (JWNLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> senses(String lemma, String pos) {
        POS wnPos = getPos(pos);
        try {
            IndexWord indexWord = dict.lookupIndexWord(wnPos, lemma);
            if (indexWord == null || indexWord.getSenses().size() == 0) {
                return new HashSet<>();
            }
            Set<String> results = new HashSet<>();
            for (Synset synset : indexWord.getSenses()) {
                int index = synset.indexOfWord(lemma.replaceAll("_", " "));
                results.add(synset.getWords().get(index).getSenseKey());
            }
            return results;
        } catch (JWNLException e) {
            throw new RuntimeException(e);
        }
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

    public static class WordNetInitializer<K extends NlpInstance> implements Supplier<ExtJwnlWordNetResource<K>>, Serializable {

        private static final long serialVersionUID = -1210563042105427915L;

        @Override
        public ExtJwnlWordNetResource<K> get() {
            return new ExtJwnlWordNetResource<>();
        }
    }

}
