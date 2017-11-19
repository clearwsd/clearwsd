package edu.colorodo.clear.wsd.feature.resource;

import com.google.common.base.Stopwatch;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.colorodo.clear.wsd.feature.util.PosUtils;
import edu.colorodo.clear.wsd.type.FeatureType;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * WordNet resource wrapping ExtJWNL (<a href="https://github.com/extjwnl/extjwnl">https://github.com/extjwnl/extjwnl</a>).
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
@NoArgsConstructor
public class ExtJwnlWordNetResource<K extends NlpInstance> implements FeatureResource<K, List<String>> {

    private static final long serialVersionUID = 4520884471486094705L;
    @Getter
    @JsonProperty
    private String key;

    private transient Dictionary dict;

    public ExtJwnlWordNetResource(String key) {
        this.key = key;
    }

    @Override
    public void initialize(InputStream inputStream) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            dict = Dictionary.getInstance(inputStream);
            log.debug("Loaded WordNet in {}.", stopwatch.toString());
        } catch (Exception e) {
            log.error("Error loading WordNet dictionary.", e);
        }
    }

    @Override
    public List<String> lookup(K key) {
        return new ArrayList<>(getHypernyms(key.feature(FeatureType.Pos), key.feature(FeatureType.Lemma)));
    }

    private Set<String> getHypernyms(String string, String pos) {
        Set<String> words = new HashSet<>();
        try {
            POS wordNetPos = getPos(pos);
            if (!POS.NOUN.equals(wordNetPos)) {
                return words;
            }
            IndexWord indexWord = dict.getIndexWord(wordNetPos, string);
            if (indexWord == null) {
                return words;
            }
            for (Synset id : indexWord.getSenses()) {
                words.addAll(words(id));
                words.addAll(PointerUtils.getDirectHypernyms(id).stream()
                        .map(s -> s.getWord().getLemma())
                        .collect(Collectors.toSet()));
            }
        } catch (Exception e) {
            log.warn("Error getting WordNet hypernyms for {}-{}", string, pos);
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
