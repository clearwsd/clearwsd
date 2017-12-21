package io.github.clearwsd.feature.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Default vocabulary implementation.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
public class BaseVocabulary implements Vocabulary {

    private static final long serialVersionUID = 5268053360096219564L;

    private BiMap<String, Integer> indices;

    @Setter
    private int defaultIndex = 0;

    public BaseVocabulary(Map<String, Integer> indices) {
        this.indices = HashBiMap.create(indices);
    }

    @Override
    public int index(String value) {
        return indices.getOrDefault(value, 0);
    }

    @Override
    public String value(int index) {
        return indices.inverse().get(index);
    }

}
