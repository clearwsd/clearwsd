package edu.colorado.clear.wsd.feature.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.NlpInstance;
import edu.colorado.clear.wsd.utils.ExtJwnlWordNet;
import edu.colorado.clear.wsd.utils.WordNetFacade;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


/**
 * WordNet feature resource.
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class WordNetResource<K extends NlpInstance> implements FeatureResource<K, List<String>> {

    public static final String WN_KEY = "WN";

    @Getter
    private String key = WN_KEY;
    @Getter
    private WordNetFacade wordNet;

    public WordNetResource(WordNetFacade wordNet) {
        this.wordNet = wordNet;
    }

    public WordNetResource() {
        this(new ExtJwnlWordNet());
    }

    @Override
    public List<String> lookup(K key) {
        return new ArrayList<>(hypernyms(key.feature(FeatureType.Lemma), key.feature(FeatureType.Pos)));
    }

    private Set<String> hypernyms(String lemma, String pos) {
        Set<String> words = new HashSet<>();
        words.addAll(wordNet.hypernyms(lemma, pos));
        words.addAll(wordNet.synonyms(lemma, pos));
        return words;
    }

    public static class WordNetInitializer<K extends NlpInstance> implements Supplier<WordNetResource<K>>, Serializable {

        private static final long serialVersionUID = -1210563042105427915L;

        @Override
        public WordNetResource<K> get() {
            return new WordNetResource<>();
        }
    }

}
