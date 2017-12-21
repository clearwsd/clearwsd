package io.github.clearwsd.feature.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

import io.github.clearwsd.feature.model.BaseVocabulary;
import io.github.clearwsd.feature.model.Vocabulary;
import io.github.clearwsd.feature.model.BaseVocabulary;
import io.github.clearwsd.feature.model.Vocabulary;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Vocabulary builder used during feature extraction to keep track of feature counts, and produce vector representations of inputs.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class VocabularyBuilder implements Vocabulary {

    private static final long serialVersionUID = 4581175527928695153L;

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
