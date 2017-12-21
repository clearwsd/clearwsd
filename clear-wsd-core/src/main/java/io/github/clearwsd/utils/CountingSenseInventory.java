package io.github.clearwsd.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Trainable sense inventory that maintains counts of each sense.
 *
 * @author jamesgung
 */
public class CountingSenseInventory implements SenseInventory, Serializable {

    private static final long serialVersionUID = -8129315817036077873L;

    private Map<String, Map<String, Integer>> inventoryMap = new HashMap<>();

    @Override
    public Set<String> senses(String lemma) {
        return inventoryMap.getOrDefault(lemma, new HashMap<>()).keySet();
    }

    @Override
    @Nullable
    public String defaultSense(String lemma) {
        Map<String, Integer> senseMap = inventoryMap.get(lemma);
        if (senseMap == null) {
            return null;
        }
        return senseMap.entrySet().stream()
                .reduce((e1, e2) -> e1.getValue() >= e2.getValue() ? e1 : e2)
                .map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public void addSense(String lemma, String sense) {
        Map<String, Integer> current = inventoryMap.computeIfAbsent(lemma, k -> new HashMap<>());
        current.merge(sense, 1, (old, one) -> old + one);
    }

}
