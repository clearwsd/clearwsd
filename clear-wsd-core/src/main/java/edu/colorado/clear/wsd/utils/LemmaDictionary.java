package edu.colorado.clear.wsd.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Lemma dictionary. Maps NLP instances onto the corresponding predicate lemma. Necessary, as lemmas at the sense level
 * are not always consistent w/ lemmas output by a lemmatizer.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class LemmaDictionary implements Function<NlpInstance, String>, Serializable {

    private static final long serialVersionUID = 126144127217207499L;

    private Map<LemmaKey, String> mappings = new HashMap<>();
    private boolean train = false;

    @Override
    public String apply(NlpInstance instance) {
        String lemma = instance.feature(FeatureType.Lemma).toString().toLowerCase();
        LemmaKey key = new LemmaKey(lemma, instance.feature(FeatureType.Pos));
        if (train) {
            String predicate = instance.feature(FeatureType.Predicate);
            if (!mappings.containsKey(key)) {
                mappings.put(key, predicate);
            }
            return predicate;
        }
        return mappings.getOrDefault(key, lemma);
    }

    @Data
    @Accessors(fluent = true)
    @AllArgsConstructor
    private static final class LemmaKey implements Serializable {

        private static final long serialVersionUID = -8067578806656259547L;

        private String form;
        private String pos;

    }

}
