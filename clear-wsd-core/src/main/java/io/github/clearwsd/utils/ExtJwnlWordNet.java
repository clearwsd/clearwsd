package io.github.clearwsd.utils;

import com.google.common.base.Stopwatch;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.feature.util.PosUtils;
import io.github.clearwsd.feature.util.PosUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link WordNetFacade} implementation, wrapping extJWNL
 * (<a href="https://github.com/extjwnl/extjwnl">https://github.com/extjwnl/extjwnl</a>).
 *
 * @author jamesgung
 */
@Slf4j
public class ExtJwnlWordNet implements WordNetFacade {

    @Getter
    private Dictionary dictionary;

    /**
     * Initialize an {@link ExtJwnlWordNet} instance with a dictionary at the provided path.
     *
     * @param inputPath path to WordNet dictionary -- if null, use default resource instance
     */
    public ExtJwnlWordNet(String inputPath) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            dictionary = inputPath == null ? Dictionary.getDefaultResourceInstance() : Dictionary.getFileBackedInstance(inputPath);
            log.debug("Loaded ExtJWNL WordNet in {}", stopwatch.toString());
        } catch (Exception e) {
            log.error("Error loading ExtJWNL WordNet dictionary", e);
        }
    }

    /**
     * Initialize {@link ExtJwnlWordNet} with default resource instance.
     */
    public ExtJwnlWordNet() {
        this(null);
    }

    @Override
    public Optional<String> mfs(String lemma, String pos) {
        List<Synset> senses = getSynsets(lemma, pos);
        if (senses.size() == 0) {
            return Optional.empty();
        }
        Synset synset = senses.get(0);
        int index = synset.indexOfWord(getLemmaString(lemma));
        Word word = synset.getWords().get(index);
        return getSenseKey(word);
    }

    @Override
    public Set<String> senses(String lemma, String pos) {
        Set<String> results = new HashSet<>();
        for (Synset synset : getSynsets(lemma, pos)) {
            int index = synset.indexOfWord(getLemmaString(lemma));
            getSenseKey(synset.getWords().get(index)).ifPresent(results::add);
        }
        return results;
    }

    @Override
    public Set<String> hypernyms(String lemma, String pos) {
        Set<String> hypernyms = new HashSet<>();
        for (Synset id : getSynsets(lemma, pos)) {
            try {
                PointerTargetNodeList directHypernyms = PointerUtils.getDirectHypernyms(id);
                hypernyms.addAll(directHypernyms.stream()
                        .map(node -> lemmas(node.getSynset()))
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()));
            } catch (JWNLException e) {
                log.warn("Error retrieving hypernyms for lemma {} and pos {}", lemma, pos);
            }
        }
        return hypernyms;
    }

    @Override
    public Set<String> synonyms(String lemma, String pos) {
        Set<String> synonyms = new HashSet<>();
        for (Synset id : getSynsets(lemma, pos)) {
            synonyms.addAll(lemmas(id));
        }
        return synonyms;
    }

    private String getLemmaString(String input) {
        return input.replaceAll("_", " ");
    }

    private List<Synset> getSynsets(String lemma, String pos) {
        return getIndexWord(lemma, pos)
                .map(IndexWord::getSenses)
                .orElse(new ArrayList<>());
    }

    private Optional<IndexWord> getIndexWord(String lemma, String pos) {
        IndexWord indexWord = null;
        try {
            POS wnPos = getPos(pos);
            if (null == wnPos) {
                return Optional.empty();
            }
            indexWord = dictionary.lookupIndexWord(wnPos, lemma);
        } catch (JWNLException e) {
            log.warn("Error retrieving index word for lemma {} and pos {}", lemma, pos, e);
        }
        return Optional.ofNullable(indexWord);
    }

    private static Optional<String> getSenseKey(Word word) {
        String result = null;
        try {
            result = word.getSenseKey();
        } catch (JWNLException e) {
            log.warn("Error retrieving sense key for word", word, e);
        }
        return Optional.ofNullable(result);
    }

    private static Set<String> lemmas(Synset synset) {
        return synset.getWords().stream()
                .map(Word::getLemma)
                .collect(Collectors.toSet());
    }

    private static POS getPos(String pos) {
        pos = pos.toUpperCase();
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
