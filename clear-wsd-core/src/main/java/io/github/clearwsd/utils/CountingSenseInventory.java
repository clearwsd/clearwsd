/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class CountingSenseInventory implements SenseInventory<String>, Serializable {

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
            return SenseInventory.DEFAULT_SENSE;
        }
        return senseMap.entrySet().stream()
                .reduce((e1, e2) -> e1.getValue() >= e2.getValue() ? e1 : e2)
                .map(Map.Entry::getKey).orElse(SenseInventory.DEFAULT_SENSE);
    }

    @Override
    public void addSense(String lemma, String sense) {
        Map<String, Integer> current = inventoryMap.computeIfAbsent(lemma, k -> new HashMap<>());
        current.merge(sense, 1, (old, one) -> old + one);
    }

    @Override
    public String getSense(String id) {
        return id;
    }

}
