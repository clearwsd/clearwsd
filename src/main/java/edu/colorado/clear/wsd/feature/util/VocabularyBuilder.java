package edu.colorado.clear.wsd.feature.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

import edu.colorado.clear.wsd.feature.model.BaseVocabulary;
import edu.colorado.clear.wsd.feature.model.Vocabulary;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Vocabulary builder used during feature extraction to keep track of feature counts, and produce vector representations of inputs.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class VocabularyBuilder implements Vocabulary {

    @Getter
    private Map<String, Integer> counts = new HashMap<>();
    @Getter
    private BiMap<String, Integer> indices = HashBiMap.create();

    public int index(String feature) {
        counts.merge(feature, 1, (old, val) -> old + val);
        return indices.computeIfAbsent(feature, none -> indices.size());
    }

    @Override
    public String value(int index) {
        return indices.inverse().get(index);
    }

    public Vocabulary build() {
        return new BaseVocabulary(indices);
    }

}